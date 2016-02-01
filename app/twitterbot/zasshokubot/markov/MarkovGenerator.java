package twitterbot.zasshokubot.markov;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.java.sen.SenFactory;
import net.java.sen.StringTagger;
import net.java.sen.dictionary.Token;

/**
 * 形態素解析とマルコフ連鎖によって新たな文章を生成するクラス
 */
public class MarkovGenerator {

	private List<String> strList = null;
	private int rank;
	private int sentenceLimit;

	/**
	 * @param strList
	 *            文章
	 * @param rank
	 *            マルコフ連鎖の階数
	 * @param sentenceLimit
	 *            生成を停止させる文字数（上限ではない）
	 */
	public MarkovGenerator(List<String> strList, int rank, int sentenceLimit) {
		if (rank < 1 || sentenceLimit <= 0 || strList == null
				|| strList.isEmpty()) {
			throw new IllegalArgumentException();
		}
		this.strList = strList;
		this.rank = rank;
		this.sentenceLimit = sentenceLimit;
	}

	/**
	 * @param rank
	 *            マルコフ連鎖の階数
	 * @param sentenceLimit
	 *            生成を停止させる文字数（上限ではない）
	 */
	public MarkovGenerator(int rank, int sentenceLimit) {
		if (rank < 1 || sentenceLimit <= 0) {
			throw new IllegalArgumentException();
		}
		this.strList = null;
		this.rank = rank;
		this.sentenceLimit = sentenceLimit;
	}

	/**
	 * 文章を生成します。<br>
	 * 失敗すると空文字を返します。<br>
	 * 元となる文章がなければ例外が投げられます。
	 * 
	 * @return 新たな文章
	 */
	public String generateSentence() {
		if (strList == null || strList.isEmpty()) {
			throw new IllegalArgumentException();
		}
		StringTagger tagger = SenFactory.getStringTagger(null);
		Random random = new Random();
		
		List<MarkovWord> wordMixs = new ArrayList<>();
		List<String> firstWord = new ArrayList<>();

		// 形態素解析にかけ、階数ごとにデータをまとめる
		for (String s : strList) {
			List<Token> tokens = new ArrayList<>();
			try {
				tagger.analyze(s, tokens);
			} catch (Exception e) {
				System.err.println("markov: " + s + "が解析できません");
				continue;
			}

			if (tokens.size() > rank && !tokens.isEmpty()) {
				firstWord.add(tokens.get(0).getSurface());
			}

			for (int i = 0; i < tokens.size() - rank; i++) {
				String pre1 = tokens.get(i).getSurface();
				String remaining = "";
				// 真ん中は全部まとめておく
				for (int j = 1; j < rank; j++) {
					remaining += tokens.get(i + j).getSurface();
				}
				String suf = tokens.get(i + rank).getSurface();

				MarkovWord wordMix = new MarkovWord(pre1, remaining, suf);
				wordMixs.add(wordMix);
			}
		}

		if (wordMixs.isEmpty()) {
			return "";
		}

		// 初期の鍵を設定
		String key = firstWord.get(random.nextInt(firstWord.size()));
		StringBuilder result = new StringBuilder(sentenceLimit);

		// 文章構築
		while (true) {

			List<MarkovWord> searchResult = this.search(wordMixs, key);
			if (searchResult.isEmpty()) {
				break;
			}

			MarkovWord wordMix = searchResult.get(random.nextInt(searchResult
					.size()));

			result.append(wordMix.getMix());
			// 上限を超えたらそこで終わり
			if (result.length() >= sentenceLimit) {
				break;
			}
			key = wordMix.getSuffix();
		}

		return result.toString();
	}

	private List<MarkovWord> search(List<MarkovWord> wordMixs, final String key) {
		List<MarkovWord> result = new ArrayList<>();
		wordMixs.stream().filter(w -> w.getPrefix1().equals(key))
				.forEach(w -> result.add(w));
		return result;
	}

	/**
	 * 文章生成の元となる文章を再設定します。
	 * 
	 * @param stringTable
	 */
	public void setStringTable(List<String> stringTable) {
		if (stringTable == null || stringTable.isEmpty()) {
			throw new IllegalArgumentException();
		}
		this.strList = stringTable;
	}
}

class MarkovWord {
	private String prefix1;
	private String mix;
	private String suffix;

	public MarkovWord(String prefix1, String remaining, String suffix) {
		this.prefix1 = prefix1;
		this.mix = prefix1 + remaining;
		this.suffix = suffix;
	}

	public String getMix() {
		return mix;
	}

	public String getPrefix1() {
		return prefix1;
	}

	public String getSuffix() {
		return suffix;
	}
}
