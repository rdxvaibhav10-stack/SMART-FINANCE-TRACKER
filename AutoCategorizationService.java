package backend.service;

import backend.dao.RuleDAO;
import backend.dao.TransactionDAO;
import backend.model.Rule;

import java.util.*;
import java.util.regex.Pattern;

public class AutoCategorizationService {
    private final RuleDAO ruleDAO;
    private final TransactionDAO txDAO;

    private List<Rule> rules = new ArrayList<>();
    private final Map<Integer, Map<String, Integer>> wordCountsByCat = new HashMap<>();
    private final Map<Integer, Integer> docCountsByCat = new HashMap<>();
    private int vocabSize = 0;
    private static final Pattern NON_WORDS = Pattern.compile("[^a-z0-9 ]");

    public AutoCategorizationService(RuleDAO ruleDAO, TransactionDAO txDAO) {
        this.ruleDAO = ruleDAO;
        this.txDAO = txDAO;
        reloadRules();
        trainFromDb();
    }

    public void reloadRules() {
        this.rules = new ArrayList<>(ruleDAO.findAll());
        this.rules.sort(Comparator.comparingInt((Rule r) -> r.getKeyword().length()).reversed());
    }

    public void trainFromDb() {
        wordCountsByCat.clear();
        docCountsByCat.clear();
        Set<String> vocab = new HashSet<>();
        for (TransactionDAO.TrainingRow r : txDAO.labeledExpenses()) {
            String[] toks = tokenize(r.text);
            docCountsByCat.merge(r.categoryId, 1, Integer::sum);
            Map<String, Integer> map = wordCountsByCat.computeIfAbsent(r.categoryId, k -> new HashMap<>());
            for (String w : toks) {
                vocab.add(w);
                map.merge(w, 1, Integer::sum);
            }
        }
        vocabSize = vocab.size();
    }

    public void observe(int categoryId, String text) {
        String[] toks = tokenize(text);
        docCountsByCat.merge(categoryId, 1, Integer::sum);
        Map<String, Integer> map = wordCountsByCat.computeIfAbsent(categoryId, k -> new HashMap<>());
        for (String w : toks) map.merge(w, 1, Integer::sum);
    }

    public Result categorize(String merchant, String note) {
        String text = ((merchant == null ? "" : merchant) + " " + (note == null ? "" : note)).trim().toLowerCase();
        text = NON_WORDS.matcher(text).replaceAll(" ").trim();
        if (text.isEmpty()) return new Result(null, false, 0.0, null);

        for (Rule r : rules) {
            String kw = r.getKeyword().toLowerCase();
            if (!kw.isEmpty() && text.contains(kw)) {
                return new Result(r.getCategoryId(), true, 1.0, kw);
            }
        }

        if (docCountsByCat.isEmpty()) return new Result(null, false, 0.0, null);
        String[] words = text.split("\\s+");
        int totalDocs = docCountsByCat.values().stream().mapToInt(i -> i).sum();
        double best = Double.NEGATIVE_INFINITY, second = Double.NEGATIVE_INFINITY;
        Integer bestCat = null;

        for (Integer cat : docCountsByCat.keySet()) {
            double logProb = Math.log((double) docCountsByCat.get(cat) / totalDocs);
            Map<String, Integer> wc = wordCountsByCat.getOrDefault(cat, Collections.emptyMap());
            int catWordCount = wc.values().stream().mapToInt(i -> i).sum();
            for (String w : words) {
                if (w.isEmpty()) continue;
                int count = wc.getOrDefault(w, 0);
                double p = (count + 1.0) / (catWordCount + vocabSize + 1e-9);
                logProb += Math.log(p);
            }
            if (logProb > best) { second = best; best = logProb; bestCat = cat; }
            else if (logProb > second) { second = logProb; }
        }
        double diff = best - second;
        double confidence = 1.0 / (1.0 + Math.exp(-diff));
        return new Result(bestCat, false, confidence, null);
    }

    private String[] tokenize(String s) {
        if (s == null) return new String[0];
        String cleaned = NON_WORDS.matcher(s.toLowerCase()).replaceAll(" ").trim();
        if (cleaned.isEmpty()) return new String[0];
        return cleaned.split("\\s+");
    }

    public static class Result {
        public final Integer categoryId;
        public final boolean viaRule;
        public final double confidence;
        public final String matchedKeyword;
        public Result(Integer categoryId, boolean viaRule, double confidence, String matchedKeyword) {
            this.categoryId = categoryId; this.viaRule = viaRule; this.confidence = confidence; this.matchedKeyword = matchedKeyword;
        }
    }
}
