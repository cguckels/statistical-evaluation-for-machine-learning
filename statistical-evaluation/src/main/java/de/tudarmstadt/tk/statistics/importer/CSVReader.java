package de.tudarmstadt.tk.statistics.importer;

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

/**
 * simple reader for tweets saved in csv files<br>
 * The {@link TweetReader} for the pipeline implements this functionality.
 * 
 * @author Karolus, Schulz
 */
public class CSVReader {

	public static ArrayList<String[]> parseSampleData(String pathToCsvFile) throws FileNotFoundException {

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
