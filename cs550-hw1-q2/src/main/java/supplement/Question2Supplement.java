package supplement;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Scanner;

public class Question2Supplement {

	public static void main(String[] args) {
		for (int i = 2; i <= 3; ++i) {
			try {
				File outputFile = new File("output/output" + i + "/part-r-00000");
				Scanner scanner = new Scanner(outputFile);
				ArrayList<RuleConfidence> ruleConfidences = new ArrayList<RuleConfidence>();
				while (scanner.hasNextLine()) {
					String[] ruleConfidence = scanner.nextLine().split("\t");
					String rule = ruleConfidence[0];
					Double confidence = Double
							.parseDouble(ruleConfidence[1].substring(0, ruleConfidence[1].length() - 1));
					ruleConfidences.add(new RuleConfidence(rule, confidence));
				}

				ruleConfidences.sort(new Comparator<RuleConfidence>() {
					@Override
					public int compare(RuleConfidence r0, RuleConfidence r1) {
						if (r0.getConfidence().equals(r1.getConfidence())) {
							String r0RuleLeft = r0.getRule().split("->")[0];
							String r1RuleLeft = r1.getRule().split("->")[0];
							return r0RuleLeft.compareTo(r1RuleLeft);
						} else if (r1.getConfidence() > r0.getConfidence()) {
							return 1;
						} else {
							return -1;
						}
					}
				});

				System.out.println("Size " + i);
				for (int j = 0; j < 5; ++j) {
					System.out.println(ruleConfidences.get(j));
				}
				System.out.println("-------------");
				scanner.close();

			} catch (FileNotFoundException e) {
				System.err.println("Error looking for output/output" + i + "part-r-00000");
			}
		}

	}

	public static class RuleConfidence {
		private String rule;
		private Double confidence;

		public RuleConfidence(String rule, Double confidence) {
			this.rule = rule;
			this.confidence = confidence;
		}

		public String getRule() {
			return rule;
		}

		public void setRule(String rule) {
			this.rule = rule;
		}

		public Double getConfidence() {
			return confidence;
		}

		public void setConfidence(Double confidence) {
			this.confidence = confidence;
		}

		@Override
		public String toString() {
			return rule + "\t" + confidence;
		}

	}

}
