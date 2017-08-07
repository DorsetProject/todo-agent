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

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;

import edu.jhuapl.dorset.ResponseStatus;
import edu.jhuapl.dorset.ResponseStatus.Code;
import edu.jhuapl.dorset.agents.AbstractAgent;
import edu.jhuapl.dorset.agents.AgentRequest;
import edu.jhuapl.dorset.agents.AgentResponse;
import edu.jhuapl.dorset.nlp.Tokenizer;
import edu.jhuapl.dorset.nlp.WhiteSpaceTokenizer;

public class ToDoListAgent extends AbstractAgent {
    private static final Logger logger = LoggerFactory.getLogger(ToDoListAgent.class);

    private static final String ADD_REGEX = ".*(ADD).*";
    private static final String REMOVE_REGEX = ".*(REMOVE).*";
    private static final String GET_REGEX = ".*(GET).*";
    private static final String ALL_REGEX = ".*(ALL).*";
    private static final String DIGIT = "[0-9]";
    private static final String DATE_FORMAT = DIGIT+DIGIT+"/"+DIGIT+DIGIT+"/"+DIGIT+DIGIT+DIGIT+DIGIT;

    private static final String NAME_KEY = "name";
    private static final String DATA_STORAGE_TYPE_KEY = "dataStorageType";

    private String name;
    private String dataStorageType;
    private ToDoListManager manager;    

    /**
     * Create a ToDoList Agent.
     * This agent creates and manipulates a user's to do list.
     * A user can add to, remove from, and get items from the to do list.
     *
     * @param config
     */
    public ToDoListAgent(Config config) {
        name = config.getString(NAME_KEY);
        dataStorageType = config.getString(DATA_STORAGE_TYPE_KEY);

        if (dataStorageType.equals("database")) {
            manager = new DBManager(name);
        } else if (dataStorageType.equals("file")){
            try {
                manager = new FileManager(name);
            } catch (ToDoListAccessException e) {
                manager = null;
            }
        } else {
            manager = null;
        }
        
    }

    /**
     * Process the user's request and determine what to do with it
     */
    public AgentResponse process(AgentRequest request) {
        if (manager == null) {
            logger.error("Could not set up manager");
            String responseMessage = "Error: Agent could not set up to do list manager";
            Code responseCode = getAgentResponseStatusCode(responseMessage);
            return createAgentResponse(responseCode, responseMessage);
        }

        String input = request.getText();
        String inputUpperCase = input.toUpperCase();

        if (inputUpperCase.matches(ADD_REGEX)) {
            input = removeAction(input, "ADD");
            return addItem(input);
        } else if (inputUpperCase.matches(REMOVE_REGEX)){
            input = removeAction(input, "REMOVE");
            return removeItem(input);
        } else if (inputUpperCase.matches(GET_REGEX)) {
            input = removeAction(input, "GET");
            return get(input);
        } else {
            logger.error("Request could not be understood: " + input);
            String responseMessage = "Error: Your request could not be understood. "
                            + "Please use one of the following in your requests: \"ADD\", \"REMOVE\", or \"GET\"";
            Code responseCode = getAgentResponseStatusCode(responseMessage);
            return createAgentResponse(responseCode, responseMessage);
        }
    }
    
    /**
     * Get Agent Response Status Code
     *
     * @param responseMessage   the message to be used to determine which code to return
     * @return the appropriate Agent Response Status Code
     */
    private Code getAgentResponseStatusCode(String responseMessage) {
        Code code;
        if (responseMessage.isEmpty()) { //.contains("Error")
            code = Code.AGENT_DID_NOT_KNOW_ANSWER;
        } else if (responseMessage.contains("manager")) {
            code = Code.AGENT_INTERNAL_ERROR;
        } else if (responseMessage.contains("not be understood")){
            code = Code.AGENT_DID_NOT_UNDERSTAND_REQUEST;
        } else {
            code = Code.SUCCESS;
        }
        return code;
    }

    /**
     * Create an Agent Response based on the code and message passed in
     *
     * @param code   the Agent Response Status Code
     * @param responseMessage   the Agent Response message
     * @return AgentResponse containing the responseMessage
     */
    private AgentResponse createAgentResponse(Code code, String responseMessage) {
        if (code.equals(Code.SUCCESS)) {
            return new AgentResponse(responseMessage);
        } else {
            return new AgentResponse(new ResponseStatus(code, responseMessage));
        }
    }

    /**
     * Remove the action word (GET, REMOVE, or ADD) from the user's input
     *
     * @param input   the user's input
     * @param action   the action word
     * @return the shortened input
     */
    private String removeAction(String input, String action) {
       int indexToStart = input.indexOf(action) + action.length() + 1;
       input = input.substring(indexToStart);
       return input;
    }

    /**
     * Add an item to the to do list
     *
     * @param input   the item to add
     * @return AgentResponse containing the item added to the to do list
     */
    private AgentResponse addItem(String input) {
        String managerResponse;
        try {
            managerResponse = manager.addItem(input);
        } catch (ToDoListAccessException e) {
           managerResponse = e.getMessage();
        }

        Code responseCode = getAgentResponseStatusCode(managerResponse);
        String responseMessage;
        if (responseCode.equals(Code.SUCCESS)) {
            responseMessage = "Item added: " + managerResponse;
        } else {
            logger.error("Item could not be added");
            responseMessage = "Error: Item could not be added";
        }

        return createAgentResponse(responseCode, responseMessage);
    }

    /**
     * Return whether the input contains an integer
     *
     * @param input   the input to check
     * @return whether the input contains an integer or not
     */
    private boolean containsInt(String input) {
        if (input.matches(DIGIT) || input.matches(DIGIT+DIGIT)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Tokenize the input by its white spaces
     *
     * @param input   the input to be tokenized
     * @return tokenizedInput   the tokenized input
     */
    private String[] tokenize(String input) {
        Tokenizer tokenizer = new WhiteSpaceTokenizer();
        String[] tokenizedInput = tokenizer.tokenize(input);
        return tokenizedInput;
    }

    /**
     * Remove an item from the to do list
     *
     * @param input   the item to remove
     * @return AgentResponse containing the item removed from the to do list
     */
    private AgentResponse removeItem(String input) {
        String managerResponse = getRemoveItemManagerResponse(input);
        
        Code responseCode = getAgentResponseStatusCode(managerResponse);
        String responseMessage;
        if (responseCode.equals(Code.SUCCESS)) {
            responseMessage = "Item removed: " + managerResponse;
        } else {
            responseMessage = "Error: Item could not be removed. Try a different item number or keyword";
        }

        return createAgentResponse(responseCode, responseMessage);
    }
    
    /**
     * Get manager response from removing an item
     *
     * @param input
     * @return the manager response message
     */
    private String getRemoveItemManagerResponse(String input) {
        try {
            if (containsInt(input)) {
                return manager.removeItem(getitemNumber(input));
            } else {
                return manager.removeItem(input);
            }
        } catch (ToDoListAccessException e) {
            return e.getMessage();
        }
    }

    /**
     * Get the item number
     *
     * @param input   the input to retrieve the item number from
     * @return the item number
     */
    private int getitemNumber(String input) {
        String[] tokenizedInput = tokenize(input);
        for (int n = 0; n < tokenizedInput.length; n++) {
            if (tokenizedInput[n].matches(DIGIT) || input.matches(DIGIT+DIGIT)) {
                return Integer.parseInt(tokenizedInput[n]);
            }
        }
        return 0;
    }

    /**
     * Determine what is to be retrieved from the to do list and get it
     *
     * @param input   what to get from the to do list
     * @return AgentResponse containing the item retrieved from the to do list
     */
    private AgentResponse get(String input) {   
        String[] tokenizedInput = tokenize(input);
        boolean containsInt = containsInt(input);
        String date = getDate(tokenizedInput);
        boolean dateIsEmpty = date.isEmpty();
        boolean containsAll = containsAll(input);
        String keyword = getKeyword(tokenizedInput);
        boolean keywordIsEmpty = keyword.isEmpty();

        if (!dateIsEmpty) {
            return getAllItemsWithKeyword(date);
        } else if (containsInt) {
            return getItem(getitemNumber(input));
        } else if (containsAll && !keywordIsEmpty) {
            return getAllItemsWithKeyword(keyword);
        } else if (containsAll) {
            return getAllText();
        } else if (!keywordIsEmpty) {
            return getItem(keyword);
        } else {
            logger.error("Request could not be understood: " + input); 
            String responseMessage = "Error: Your request could not be understood.";
            Code responseCode = getAgentResponseStatusCode(responseMessage);
            return createAgentResponse(responseCode, responseMessage);
        }
    }

    /**
     * Return whether the input contains "ALL"
     *
     * @param input   the input to check
     * @return whether the input contains "ALL"
     */
    private boolean containsAll(String input) {
        if (input.toUpperCase().matches(ALL_REGEX)) {
            return true;
        }
        return false;
    }

    /**
     * Get the date from the tokenized input
     *
     * @param tokenizedInput   the input to check
     * @return date   the date from the input or an empty string
     */
    private String getDate(String[] tokenizedInput) {
        String date = "";
        for (int n = 0; n < tokenizedInput.length; n++) {
            if (tokenizedInput[n].matches(DATE_FORMAT)) {
                date = tokenizedInput[n];
            }
        }
        return date;
    }

    /**
     * Get the keyword from the tokenized input
     *
     * @param tokenizedInput   the input to check
     * @return keyword   the keyword from the input or an empty string
     */
    private String getKeyword(String[] tokenizedInput) {
        String keyword = "";
        for (int n = 0; n < tokenizedInput.length; n++) {
            if (!tokenizedInput[n].matches(DIGIT) && !tokenizedInput[n].toUpperCase().matches(ALL_REGEX)) {
                if (!keyword.isEmpty()) {
                    keyword += " ";
                }
                keyword += tokenizedInput[n];
            }
        }
        return keyword;
    }

    /**
     * Get all the text from the to do list
     *
     * @return AgentResponse containing the to do list text
     */
    private AgentResponse getAllText() {
        ArrayList<String> managerResponseList;
        String managerResponse;
        try {
            managerResponseList = manager.getAllText();
            managerResponse = joinList(managerResponseList);
        } catch (ToDoListAccessException e) {
            managerResponse = e.getMessage();
        }
        
        Code responseCode = getAgentResponseStatusCode(managerResponse);
        String responseMessage;
        if (responseCode.equals(Code.SUCCESS)) {
            responseMessage = managerResponse;
        } else {
            logger.error("Could not retrieve text");
            responseMessage = "Error: Could not retrieve text";
        }

        return createAgentResponse(responseCode, responseMessage);
    }

    /**
     * Combine the contents of an ArrayList into one string
     *
     * @param list   the Arraylist to be combined
     * @return text   the contents of the combined Arraylist
     */
    private String joinList(ArrayList<String> list) {
        String text = "";
        for (int n = 0; n < list.size(); n++) {
            text += list.get(n) + "\n";
        }
        return text;
    }

    /**
     * Get all items from the to do list containing a keyword
     *
     * @param keyword   the keyword to find the items
     * @return AgentResponse containing the items retrieved from the to do list
     */
    private AgentResponse getAllItemsWithKeyword(String keyword) {
        ArrayList<String> managerResponseList;
        String managerResponse;
        try {
            managerResponseList = manager.getAllItemsWithKeyword(keyword);
            managerResponse = joinList(managerResponseList);
        } catch (ToDoListAccessException e) {
            managerResponse = e.getMessage();
        }

        Code responseCode = getAgentResponseStatusCode(managerResponse);
        String responseMessage;
        if (responseCode.equals(Code.SUCCESS)) {
            responseMessage = managerResponse;
        } else {
            responseMessage = "Error: No items matched your keyword";
        }

        return createAgentResponse(responseCode, responseMessage);
    }

    /**
     * Get an item from the to do list
     *
     * @param input   the item to get from the to do list
     * @return AgentResponse containing the item retrieved from the to do list
     */
    private AgentResponse getItem(int itemNumber) {
        String managerResponse;
        try {
            managerResponse = manager.getItem(itemNumber);
        } catch (ToDoListAccessException e) {
            managerResponse = e.getMessage();
        }

        Code responseCode = getAgentResponseStatusCode(managerResponse);
        String responseMessage;
        if (responseCode.equals(Code.SUCCESS)) {
            responseMessage = managerResponse;
        } else {
            responseMessage = "Error: Item could not be found";
        }

        return createAgentResponse(responseCode, responseMessage);
    }

    /**
     * Get an item from the to do list
     *
     * @param input   the item to get from the to do list
     * @return AgentResponse containing the item retrieved from the to do list
     */
    private AgentResponse getItem(String input) {
        String managerResponse;
        try {
            managerResponse = manager.getItem(input);
        } catch (ToDoListAccessException e) {
            managerResponse = e.getMessage();
        }

        Code responseCode = getAgentResponseStatusCode(managerResponse);
        String responseMessage;
        if (responseCode.equals(Code.SUCCESS)) {
            responseMessage = managerResponse;
        } else {
            responseMessage = "Error: Item could not be found";
        }

        return createAgentResponse(responseCode, responseMessage);
    }
}
