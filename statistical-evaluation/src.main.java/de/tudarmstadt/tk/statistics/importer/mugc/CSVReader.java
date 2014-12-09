package de.tudarmstadt.tk.statistics.importer.mugc;

/**
 * Copyright 2014
 * Telecooperation (TK) Lab
 * Technische Universit�t Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.Vector;

/**
 * simple reader for tweets saved in csv files<br>
 * The {@link TweetReader} for the pipeline implements this functionality.
 * 
 * @author Karolus, Schulz
 *
 */
public class CSVReader {

	/**
	 * parses a given csv file (given the path) and produces a {@link List} of
	 * {@link Tweet}s<br>
	 * Format: id; label; text (no first line given the column identifiers!)
	 * 
	 * @param pathToCsvFile
	 *            complete path to csv file (including file suffix)
	 * @return {@link List} of {@link Tweet}, which are yet to be preprocessed
	 * @throws FileNotFoundException
	 */
	public static List<Tweet> parseTweetData(String pathToCsvFile) throws FileNotFoundException {

		List<Tweet> tweets = new Vector<Tweet>();
		BufferedReader bin = new BufferedReader(new FileReader(new File(pathToCsvFile)));

		// we use two scanners to stabilize the reading process
		// this way a failure in one tweet does not corrupt the others
		Scanner outerScanner = new Scanner(bin);
		outerScanner.useDelimiter("\\r\\n|\\n");

		while (outerScanner.hasNext()) {

			Scanner innerScanner = new Scanner(outerScanner.next());
			innerScanner.useDelimiter(";");

			long id = innerScanner.nextLong();
			String text = innerScanner.next();
			String label = innerScanner.next();

			// if we fail to separate, e.g. if a ; occurs in the tweet itself,
			// we discard it for now
			Tweet tweet = new Tweet(id, removeNonAlphabetics(text), label.toLowerCase(Locale.ENGLISH));
			tweets.add(tweet);

			innerScanner.close();
		}
		outerScanner.close();

		return tweets;

	}

	public static ArrayList<String[]> parseReportData(String pathToCsvFile) throws FileNotFoundException {

		ArrayList<String[]> data = new ArrayList<String[]>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(pathToCsvFile)));

			String separator = ",\"";
			String[] header = reader.readLine().split(separator, -1);
			int nColumns = header.length;

			String line;
			while ((line = reader.readLine()) != null) {
				String[] tokens = line.split(separator, -1);

				for (int i = 0; i < tokens.length; i++) {
					tokens[i] = tokens[i].replace("\"", "");
				}

				data.add(tokens);
				if (nColumns != tokens.length) {
					System.err.println(".csv file corrup: number of columns not same for each row.");
					return null;
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return data;
	}

	/**
	 * converts a {@link List} of {@link Tweet} to {@link ClassSensitiveEntity}
	 * 
	 * @param tweets
	 * @return {@link List} of {@link ClassSensitiveEntity}
	 */
	public static List<ClassSensitiveEntity> convertTweets(List<Tweet> tweets) {
		List<ClassSensitiveEntity> entities = new Vector<>();
		for (Tweet t : tweets)
			entities.add(t);

		return entities;
	}

	public static String removeNonAlphabetics(String unrefinedText) {
		// matches letters in basic Latin (InBasic_Latin), numbers (N),
		// punctuation (P) and math as well as currency symbols (S)
		// we also remove "_" (Unicode 5F) as apparently the segmentizer has
		// problem with it
		// DOES NOT match: umlauts (ä,ö,ü) or other letters common to other
		// language than english (accents, reverted question mark, etc.)
		String cleaned = unrefinedText.replaceAll("[^\\p{InBasic_Latin}\\p{N}\\p{Z}\\p{P}\\p{Sm}\\p{Sc}[\\x5F]]", "");
		// remove URLs too
		String cleanedURLs = cleaned.replaceAll("(http://\\S*)", "url").replaceAll("''", "'");
		return cleanedURLs;

	}

}
