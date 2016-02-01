package twitterbot.zasshokubot.markov;

import java.util.List;

class MarkovThread extends Thread {
	private MarkovGenerator markov;
	private String result = null;
	private boolean threadRunning;

	public MarkovThread(List<String> textList, int rank, int sentenceLimit) {
		super();
		markov = new MarkovGenerator(rank, sentenceLimit);
		markov.setStringTable(textList);
		threadRunning = false;
	}

	public void stopThread() {
		threadRunning = false;
	}

	public String getResult() {
		return result;
	}

	@Override
	public void run() {
		threadRunning = true;
		while (threadRunning) {
			result = markov.generateSentence();
			threadRunning = false;
		}
	}
}
