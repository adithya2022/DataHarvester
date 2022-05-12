package com.data.harvest;

import static java.util.Map.entry;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadCSV {
	public static void main(String args[]) throws IOException {
		
		final Logger logger = LoggerFactory.getLogger(ReadCSV.class);
		// HashMap created for crop codes
		Map<String, String> cropCodes = Map.ofEntries(entry("W", "Wheat"), entry("B", "Barley"), entry("M", "Maize"),
				entry("BE", "Beetroot"), entry("C", "Carrot"), entry("PO", "Potatoes"), entry("PA", "Parsnips"),
				entry("O", "Oats")

		);
		final String fileName = "..\\DataHarvester\\Input\\harvest data - validation needed.csv";//validation csv
	//	final String fileName = "..\\DataHarvester\\Input\\harvest data - clean.csv";//actual csv
		FileWriter writer = new FileWriter("..\\DataHarvester\\Output\\DataHarvesterOutput.csv");
		String csvLine;
		BufferedReader br;

		File f = new File(fileName);
		br = new BufferedReader(new FileReader(f));
		try {

			while ((csvLine = br.readLine()) != null) {
				Boolean isError=false;
				List<String> finalList = new ArrayList<String>();

				String[] data = csvLine.split(",");
				
				String county = data[0];

				String[] newDataSet = removeFirstElement(data);
				List<String> inputList = Arrays.asList(newDataSet);

				finalList.add(county);

				// code for grouping cropcodes and quantities separately 
				List<List<String>> groups = groupingCropCodesAndQuality(inputList);

				// crop code for corresponding county
				List<String> cropCode = groups.get(0);
				for(String code:cropCode)
				{
					if(!cropCodes.containsKey(code.trim()))
					{
						logger.error("Crop code does not exist "+code.trim());
									isError=true;		
						}
				}
				
				// quantity for corresponding county
				List<String> quantityList = groups.get(1);
				
				for(String quantity:quantityList)
				{
					if(!quantity.trim().matches("[0-9]+"))
					{
						logger.error("Quantity Error "+quantity.trim());
						isError=true;
					}
				}
				if(isError)
				continue;
				List<Double> quantity = new ArrayList<Double>();
				for (int a = 0; a < quantityList.size(); a++) {
					
					quantity.add(Double.parseDouble(quantityList.get(a).trim()));
				}

				List<Double> percentageArray = findPercentage(quantity);

				for (int j = 0; j < percentageArray.size(); j++) {
					finalList.add(cropCodes.get(cropCode.get(j).trim()));
					finalList.add(percentageArray.get(j).toString() + "%");
				}
				finalList.add("\n");
				System.out.println(finalList);
				System.out.println("\n");
				String collect = finalList.stream().collect(Collectors.joining(","));
				System.out.println(collect);
				writer.write(collect);
			}
			br.close();
			writer.close();

		} catch (Exception e) {
			e.printStackTrace();
		}


	}

	public static String[] removeFirstElement(String[] arr) {
		String newArr[] = new String[arr.length - 1];
		for (int i = 1; i < arr.length; i++) {
			newArr[i - 1] = arr[i];
		}
		return newArr;
	}

	public static List<List<String>> groupingCropCodesAndQuality(List<String> inputList) {
		List<List<String>> groups = IntStream.range(0, 2).mapToObj(m -> new ArrayList<String>())
				.collect(Collectors.toList());
		for (int k = 0; k < inputList.size(); k++) {
			groups.get(k % 2).add(inputList.get(k));
		}
		return groups;
	}

	public static List<Double> findPercentage(List<Double> quantity) {
		List<Double> percentageArray = new ArrayList<Double>();
		Double sum = quantity.stream().mapToDouble(a -> a).sum();
		for (Double d : quantity) {
			Double percentage = (double) Math.round((d / sum) * 100);
			percentageArray.add(percentage);
		}
		return percentageArray;
	}
}
