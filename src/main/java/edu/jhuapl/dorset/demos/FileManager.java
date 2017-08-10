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

public class FileManager implements ToDoListManager {

    private static final int TITLE_LINE = 0;
    private File file;
    private String toDoListName;

    /**
     * Create a FileManager
     *
     * @param toDoListName  the name of the ToDo list
     * @throws ToDoListAccessException  if the file cannot be created or opened
     */
    public FileManager(String toDoListName) throws ToDoListAccessException {
        file = new File("./" + toDoListName + ".csv");
        this.toDoListName = toDoListName;

        try {
            if (!file.exists()) {
                file.createNewFile();
                writeTitle(file);
            }
        } catch (IOException | ToDoListAccessException e) {
            throw new ToDoListAccessException("Could not create file", e);
        }
    }

    /**
     * Write the title of the file
     *
     * @param file  the toDo list file
     * @throws ToDoListAccessException  if the file exists but is a directory rather than a file or cannot be opened
     */
    private void writeTitle(File file) throws ToDoListAccessException {
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            bufferedWriter.write(toDoListName + " TODO List");
            bufferedWriter.close();
        } catch (IOException e) {
            throw new ToDoListAccessException("Could not write title", e);
        }
    }

    /**
     * Add an item to the ToDo list file
     *
     * @param item  the item to add
     * @throws ToDoListAccessException  if the toDo list cannot be accessed
     */
    public String addItem(String item) throws ToDoListAccessException {
        int nextNumber;
        try {
            nextNumber = getAllText().size();
        } catch (ToDoListAccessException e) {
            throw new ToDoListAccessException(e.getMessage(), e);
        }

        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file, true))) {
            bufferedWriter.write("\n" + nextNumber + ")," + getDate() + "," + getTime() + "," + item);
            return item;
        } catch (IOException e) {
            throw new ToDoListAccessException("Item could not be added: " + item, e);
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
     * @param itemNumber  the number of the item to be retrieved
     * @return the item removed
     * @throws ToDoListAccessException  if the item cannot be removed
     */
    public String removeItem(int itemNumber) throws ToDoListAccessException {
        try {
            return removeItem(itemNumber + "),");
        } catch (ToDoListAccessException e) {
            throw new ToDoListAccessException(e.getMessage(), e);
        }
    }

    /**
     * Remove an item from the ToDo list file
     *
     * @param itemKeyword  the keyword to find the item
     * @return the item removed
     * @throws ToDoListAccessException  if the item cannot be removed
     */
    public String removeItem(String itemKeyword) throws ToDoListAccessException {
        try {
            ArrayList<String> text = getAllText();
            return rewriteFileWithoutRemoved(text, itemKeyword);
        } catch (ToDoListAccessException e) {
            throw new ToDoListAccessException(e.getMessage(), e);
        }
    }

    /**
     * Rewrite the ToDo list file without the item indicated by itemKeyword.
     *
     * @param text  the text from the ToDo list file
     * @param itemKeyword  the keyword to find the item
     * @return lineRemoved  the line removed from the ToDo list file
     * @throws ToDoListAccessException  if the item cannot be removed
     */
    private String rewriteFileWithoutRemoved(ArrayList<String> text, String itemKeyword) throws ToDoListAccessException {
        int counter = 0;
        String lineRemoved = "";
        itemKeyword = itemKeyword.toLowerCase();

        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file))) {
            for (int n = 0; n < text.size(); n++) {
                String itemN = text.get(n);
                boolean textContains = itemN.toLowerCase().contains(itemKeyword);
                if (!textContains) {
                    rewriteItem(itemN, bufferedWriter, counter);
                    counter++;
                } else {
                    lineRemoved = itemN;
                }
            }

            if (lineRemoved.isEmpty()) {
                return null;
            }
        } catch (IOException e) {
            throw new ToDoListAccessException("Item could not be removed", e);
        }
        return lineRemoved;
    }

    /**
     * Write items back into file
     *
     * @param text  the item text to add
     * @param bufferedWriter  writes the text to a character-output stream
     * @param counter  the item number
     * @throws ToDoListAccessException  if an error occurs while writing text
     */
    private void rewriteItem(String text, BufferedWriter bufferedWriter, int counter) throws ToDoListAccessException {
        try {
            if (counter == TITLE_LINE) {
                bufferedWriter.write(text);
            } else {
                int indexToStart = getIndexToStartItem(text);
                String item = "\n" + counter + ")," + text.substring(indexToStart);
                bufferedWriter.write(item);
            }
        } catch (IOException e) {
            throw new ToDoListAccessException(e.getMessage(), e);
        }
    }

    /**
     * Get the index where the item text starts.
     * Occurs after "#),"
     *
     * @param item  the item
     * @return the index where the item text starts
     */
    private int getIndexToStartItem(String item) {
        return item.indexOf("),") + 2;
    }

    /**
     * Get all the text from the ToDo list file
     *
     * @return text  the text from the ToDo list file
     * @throws ToDoListAccessException  if the toDo list cannot be accessed
     */
    public ArrayList<String> getAllText() throws ToDoListAccessException {
        ArrayList<String> text = new ArrayList<String>();

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            String currentLine;
            while ((currentLine = bufferedReader.readLine()) != null) {
                text.add(currentLine);
            }
        } catch (IOException e) {
            throw new ToDoListAccessException("Could not retrieve text");
        }
        return text;
    }

    /**
     * Get all the items with the keyword.
     *
     * @param itemKeyword  the keyword to find the items
     * @return itemsWithKeyword  a list of items with the keyword
     * @throws ToDoListAccessException  if toDo list cannot be accessed
     */
    public ArrayList<String> getAllItemsWithKeyword(String itemKeyword) throws ToDoListAccessException {
        itemKeyword = itemKeyword.toLowerCase();  
        
        ArrayList<String> allText;
        try {
            allText = getAllText();
        } catch (ToDoListAccessException e) {
            throw new ToDoListAccessException(e.getMessage(), e);
        }
        ArrayList<String> itemsWithKeyword = new ArrayList<String>();

        for (int n = 1; n < allText.size(); n++) {
            String currentLine = allText.get(n).toLowerCase();
            if (currentLine.contains(itemKeyword)) {
                itemsWithKeyword.add(allText.get(n));
            }
        }

        return itemsWithKeyword;
    }

    /**
     * Get the item based on the item number
     *
     * @param itemNumber  the number of the item to be retrieved
     * @return the item retrieved
     * @throws ToDoListAccessException  if the item cannot be retrieved
     */
    public String getItem(int itemNumber) throws ToDoListAccessException {
        try {
            return getItem(itemNumber + "),");
        } catch (ToDoListAccessException e) {
            throw new ToDoListAccessException(e.getMessage(), e);
        } 
    }

    /**
     * Get the item based on a keyword.
     * If there are two or more items with the keyword, the first in the list will be returned.
     *
     * @param itemKeyword  a keyword to find the items
     * @return the item containing the keyword
     * @throws ToDoListAccessException  if the toDo list cannot be accessed
     */
    public String getItem(String itemKeyword) throws ToDoListAccessException {
        itemKeyword = itemKeyword.toLowerCase();  
        
        ArrayList<String> allText;
        try {
            allText = getAllText();
        } catch (ToDoListAccessException e) {
            throw new ToDoListAccessException("Could not retrieve text", e);
        }

        for (int n = 0; n < allText.size(); n++) {
            String currentLine = allText.get(n).toLowerCase();
            if (currentLine.contains(itemKeyword)) {
                return allText.get(n);
            }
        }
        return null;
    }
}
