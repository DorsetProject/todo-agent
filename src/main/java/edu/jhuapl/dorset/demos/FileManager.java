/*
 * Copyright 2017 The Johns Hopkins University Applied Physics Laboratory LLC
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.jhuapl.dorset.demos;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileManager implements ToDoListManager {
    private static final Logger logger = LoggerFactory.getLogger(FileManager.class);
    
    private static final int TITLE_LINE = 0;
    private File file;
    private String toDoListName;

    /**
     * Create a FileManager
     *
     * @param toDoListName   the name of the ToDo list
     * @throws IOException 
     */
    public FileManager(String toDoListName) throws IOException {
        file = new File("./" + toDoListName + ".csv");
        this.toDoListName = toDoListName;

        try {
            if (!file.exists()) {
                file.createNewFile();
                writeTitle(file, "'s TODO List:");
            }
        } catch (IOException e) {
            logger.error("Could not create file"); //TODO
            throw new IOException("Could not create file", e);
        }
    }

    /**
     * Write the title of the file
     *
     * @throws IOException   if the file exists but is a directory rather
     * than a regular file or cannot be opened for some reason
     */
    private void writeTitle(File file, String titleExtension) {
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            bufferedWriter.write(toDoListName + titleExtension);
            bufferedWriter.close();
        } catch (IOException e) {
            logger.error("Could not write title" + e);
        }
    }

    /**
     * Add an item to the ToDo list file
     *
     * @param item   the item to add
     * @throws IOException
     */
    public String addItem(String item) {
        ArrayList<String> text = getAllText();

        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file, true))) {
            bufferedWriter.write("\n" + text.size() + ")," + getDate() + "," + getTime() + "," + item);
            return item;
        } catch (IOException e) {
            logger.error("Item could not be added: " + item);
            return "Error";
        }
    }

    /**
     * Get the current date
     * Formatted as: MM/dd/yyyy
     *
     * @return the current date
     */
    private String getDate() {
        DateFormat day = new SimpleDateFormat("MM/dd/yyyy");
        return day.format(new Date());
    }

    /**
     * Get the current time
     * Formatted as: hh:mm AM/PM
     *
     * @return the current time
     */
    private String getTime() {
        DateFormat time = DateFormat.getTimeInstance(DateFormat.SHORT);
        return time.format(new Date());
    }

    /**
     * Remove an item from the ToDo list file
     *
     * @param itemNumber   the item number
     * @return the item removed
     * @throws IOException   if the item cannot be removed
     */
    public String removeItem(int itemNumber) {
        return removeItem(itemNumber + "),");
    }

    /**
     * Remove an item from the ToDo list file
     *
     * @param itemKeyword   the keyword to find the item
     * @return the item removed
     * @throws IOException   if the item cannot be removed
     */
    public String removeItem(String itemKeyword) {
        ArrayList<String> text = getAllText();
        return rewriteFileWithoutRemoved(text, itemKeyword);
    }

    /**
     * Rewrite the ToDo list file without the item indicated by itemKeyword.
     * This method is case sensitive
     *
     * @param text   the text from the ToDo list file
     * @param itemKeyword   the keyword to find the item
     * @return lineRemoved   the line removed from the ToDo list file
     */
    private String rewriteFileWithoutRemoved(ArrayList<String> text, String itemKeyword) {
        int counter = 0;
        String lineRemoved = "";

        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file))) {
            for (int n = 0; n < text.size(); n++) {
                String itemN = text.get(n);
                boolean textContains = itemN.contains(itemKeyword);
                if (!textContains) {
                    rewriteItem(itemN, bufferedWriter, counter);
                    counter++;
                } else {
                    lineRemoved = itemN;
                }
            }

            if (lineRemoved.isEmpty()) {
                return "Error";
            }
        } catch (IOException e) {
            logger.error("Item could not be removed");
            return "Error";
        }
        return lineRemoved;
    }

    /**
     * Write items back into file
     *
     * @param text   the item to add
     * @param bufferedWriter   writes the text to a character-output stream
     * @param counter   the item number
     * @throws IOException   if an I/O error occurs while writing text
     */
    private void rewriteItem(String text, BufferedWriter bufferedWriter, int counter) throws IOException {
        try {
            if (counter == TITLE_LINE) {
                bufferedWriter.write(text);
            } else {
                int indexToStart = getIndexToStartItem(text);
                String item = "\n" + counter + ")," + text.substring(indexToStart);
                bufferedWriter.write(item);
            }
        } catch (IOException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    /**
     * Get the index where the item text starts.
     * Occurs after "#),"
     *
     * @param item   the item
     * @return the index where the item text starts
     */
    private int getIndexToStartItem(String item) {
        return item.indexOf("),") + 2;
    }

    /**
     * Get all the text from the ToDo list file
     *
     * @return text   the text from the ToDo list file
     */
    public ArrayList<String> getAllText() {
        ArrayList<String> text = new ArrayList<String>();

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            String currentLine;
            while((currentLine = bufferedReader.readLine()) != null) {
                text.add(currentLine);
            }
        } catch (IOException e) {
            logger.error("Could not retrieve text");
            text.add("Error");
        }
        return text;
    }

    /**
     * Get all the items with the keyword.
     * This method is case sensitive
     *
     * @param itemKeyword   the keyword to find the items
     * @return itemsWithKeyword   a list of items with the keyword
     */
    public ArrayList<String> getAllItemsWithKeyword(String itemKeyword) {
            ArrayList<String> allText = getAllText();
            ArrayList<String> itemsWithKeyword = new ArrayList<String>();

            for (int n = 1; n < allText.size(); n++) {
                if (allText.get(n).contains(itemKeyword)) {
                    itemsWithKeyword.add(allText.get(n));
                }
            }

            if (itemsWithKeyword.isEmpty()) {
                itemsWithKeyword.add("Error");
            }
            return itemsWithKeyword;
     }

    /**
     * Get the item based on the item number
     *
     * @param itemNumber   the item number
     * @return getItem(String itemKeyword)
     */
    public String getItem(int itemNumber) {
        return getItem(itemNumber +"),");
    }

    /**
     * Get the item based on a keyword.
     * If there are two or more items with the keyword, the first in the list will be returned.
     * This method is case sensitive
     *
     * @param itemKeyword   a keyword to find the items
     * @return the item containing the keyword
     */
    public String getItem(String itemKeyword) {
        ArrayList<String> text = getAllText();

        for (int n = 0; n < text.size(); n++) {
            if (text.get(n).contains(itemKeyword)) {
                return text.get(n);
            }
        }
        return "Error";
    }
}
