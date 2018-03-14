package edu.arizona.cs;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import edu.stanford.nlp.simple.Document;

/**
 * Program Intention: A Query Likelihood Language Model Implementation Build:
 * Maven Language Processor: coreNLP from Stanford University Programmer: YANG
 * HONG
 * 
 */

public class App {

	private static List<String> queryList;
	private static List<DocAndScore> rankList;
	private static List<Document> docList;
	
//	private static Document doc1 = new Document("information retrieval is the most awesome class I ever took.");
//	private static Document doc2 = new Document("the retrieval of private information from your emails is a job that the NSA loves.");
//	private static Document doc3 = new Document("in the school of information you can learn about data science.");
//	private static Document doc4 = new Document("the labrador retriever is a great dog.");
	
	private static Document doc1 = new Document("click go the shears boys click click click");
	private static Document doc2 = new Document("click click");
	private static Document doc3 = new Document("metal here");
	private static Document doc4 = new Document("metal shears click here");
	
	private static NumberFormat formatter;

	private static class DocAndScore {
		private int docID;
		private double score;
		
		public DocAndScore(int docID, double score){
			this.docID = docID;
			this.score = score;
		}
		
		private int getDocID(){
			return this.docID;
		}
		
		private double getScore(){
			return this.score;
		}
	}
	
	public static void main(String[] args) {
		docList = new ArrayList<Document>();
		docList.add(doc1);
		docList.add(doc2);
		docList.add(doc3);
		docList.add(doc4);

		print("************ Begin ************");

		@SuppressWarnings("resource")
		Scanner keyboard = new Scanner(System.in);
		String choice = "y";
		double factor = 0.5;
		formatter = new DecimalFormat("#0.0000000000");

		while (choice.equals("y")) {
			print("------------------------------");
			print("Please enter a query:");
			// get query from stdin
			String query = keyboard.nextLine();
			// Run language processing on query
			processQuery(query);
			print("newline");

//			for (int i = 0; i < queryList.size(); i++)
//				print(queryList.get(i));
			//print();
			print("No smoothing:");
			rankList = new ArrayList<DocAndScore>();
			rankList.add(new DocAndScore(1, getNoSmoothing(doc1)));
			rankList.add(new DocAndScore(2, getNoSmoothing(doc2)));
			rankList.add(new DocAndScore(3, getNoSmoothing(doc3)));
			rankList.add(new DocAndScore(4, getNoSmoothing(doc4)));
			sortRankList();
			printRankList();
			print("newline");
			
			print("Jelinek-Mercer smoothing:");
			System.out.print("Please enter the weighting factor(0 - 1): ");
			factor = keyboard.nextDouble();
			keyboard.nextLine();
			rankList = new ArrayList<DocAndScore>();
			rankList.add(new DocAndScore(1, getMercerSmoothing(doc1, factor)));
			rankList.add(new DocAndScore(2, getMercerSmoothing(doc2, factor)));
			rankList.add(new DocAndScore(3, getMercerSmoothing(doc3, factor)));
			rankList.add(new DocAndScore(4, getMercerSmoothing(doc4, factor)));
			sortRankList();
			printRankList();
			print("newline");

			System.out.print("Try another query? (y/n) ");
			choice = keyboard.nextLine().toLowerCase();
		}

		print("************* End *************");
	}

	private static void printRankList() {
		for (DocAndScore doc : rankList)
			System.out.println("Document #" + doc.getDocID() + ": " + formatter.format(doc.getScore()));
	}

	private static void sortRankList() {
		List<DocAndScore> temp = new ArrayList<DocAndScore>();
		int originalSize = rankList.size();
		DocAndScore max = rankList.get(0);
		
		for (int i = 0; i < originalSize - 1; i++){
			for (int n = 0; n < rankList.size(); n++){
				if (rankList.get(n).getScore() > max.getScore())
					max = rankList.get(n);
			}
			temp.add(max);
			rankList.remove(max);
			max = rankList.get(0);
		}
		
		temp.add(rankList.get(0));
		rankList = temp;
	}

	private static double getNoSmoothing(Document doc) {
		int count = 0;
		int docLength = doc.sentence(0).length();
		double result = 1;
		
		for (String lemma : queryList){
			for (int i = 0; i < docLength; i++){
				if (lemma.equals(doc.sentence(0).lemma(i)))
					count++;
			}
			//System.out.println(lemma + ": " + count + " / " + docLength);
			result = result * ((double)count / (double)docLength);
			count = 0;
		}
		
		return result;
	}
	
	private static double getMercerSmoothing(Document doc, double factor) {
		int docLength = doc.sentence(0).length();
		int totalLength = 0;
		for(Document d : docList)
			totalLength += d.sentence(0).length();
		
		int countInDoc = 0;
		int countInCol = 0;
		double PtMd = 0;
		double PtMc = 0;
		double result = 1;
		int i, n;
		
		for (String lemma : queryList) {

			for (i = 0; i < docLength; i++)
				if (lemma.equals(doc.sentence(0).lemma(i)))
					countInDoc++;
			//printInt(countInDoc);

			for (n = 0; n < docList.size(); n++) {
				for (i = 0; i < docList.get(n).sentence(0).length(); i++)
					if (lemma.equals(docList.get(n).sentence(0).lemma(i)))
						countInCol++;
			}
			//printInt(countInCol);
			
			PtMd = factor * ((double)countInDoc / (double)docLength);
			PtMc = (1 - factor) * ((double)countInCol / (double)totalLength);
			result = result * (PtMd + PtMc);
			countInDoc = 0;
			countInCol = 0;
		}
		
		return result;
	}

	private static void processQuery(String query) {
		// create a query document
		Document queryDoc = new Document(query);
		// create/re-create a query containing all terms as a list
		queryList = new ArrayList<String>();
		
		for (int i = 0; i < queryDoc.sentence(0).length(); i++)
			queryList.add(queryDoc.sentence(0).lemma(i));
	}

	private static void print(String toBePrinted) {
		if (toBePrinted.equals("newline"))
			System.out.println();
		else
			System.out.println(toBePrinted);
	}

	private static void printInt(int num) {
		System.out.println(num);
	}
}
