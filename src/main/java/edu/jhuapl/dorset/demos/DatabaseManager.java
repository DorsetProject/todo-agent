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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Restrictions;

public class DatabaseManager implements ToDoListManager {

    private SessionFactory factory;

    /**
     * Create a DB Manager
     *
     * @throws ToDoListAccessException 
     */
    public DatabaseManager() throws ToDoListAccessException {
        try {
            Configuration configuration = new Configuration().configure();
            factory = configuration.buildSessionFactory();
        } catch (HibernateException e) {
            throw new ToDoListAccessException("Invalid hibernate configuration. See sample.cfg.xml");
        }
    }

    /**
     * Add an item to the database
     *
     * @return item  the item to add
     */
    public String addItem(String item) {        
        Session session = getSession();

        Item todoItem = createItem(item);
        session.save(todoItem);

        endSession(session);
        return item; 
    }
    
    /**
     * Create and get a new Session
     *
     * @return session  the new Session
     */
    private Session getSession() {
        Session session = factory.openSession();
        session.beginTransaction();
        return session;
    }

    /**
     * Commit and close current Session
     *
     * @param session  the session to close
     */
    private void endSession(Session session) {
        session.getTransaction().commit();
        session.close();
    }

    /**
     * Create a new item with given task
     *
     * @param task  the task for the new item
     * @return todoItem  the new item to be added
     */
    private Item createItem(String task) {
        Item todoItem = new Item();

        int listNumber = getItemCount() + 1;
        todoItem.setListNumber(listNumber);
        todoItem.setTask(task);
        todoItem.setDateCreated(getDate());
        todoItem.setTimeCreated(getTime());

        return todoItem;
    }

    /**
     * Get the number of items in the database
     *
     * @return itemCount  the number of items in the database
     */
    private int getItemCount() {
        Session session = getSession();

        String hql = "SELECT COUNT(item_id) FROM " + Item.class.getName();
        Query query = session.createQuery(hql);
        int itemCount = getQueryResponseItemCount(query);
        
        endSession(session);
        return itemCount;
    }

    /**
     * Get response from query
     *
     * @param query  the query request
     * @return itemCount  the query response
     */
    private int getQueryResponseItemCount(Query query) {
        String response = query.list().toString();
        response = response.substring(1, response.indexOf("]"));
        int itemCount = Integer.parseInt(response);

        return itemCount;
    }

    /**
     * Get the current date
     * Formatted as: MM/dd/yyyy
     *
     * @return the current date
     */
    private String getDate() {
        Date now = new Date();
        DateFormat day = new SimpleDateFormat("MM/dd/yyyy");
        return day.format(now);
    }

    /**
     * Get the current time
     * Formatted as: hh:mm AM/PM
     *
     * @return the current time
     */
    private String getTime() {
        Date now = new Date();
        DateFormat time = DateFormat.getTimeInstance(DateFormat.SHORT);
        return time.format(now);
    }

    /**
     * Remove an item from the database based on its item number
     * 
     * @param itemNumber  the number of the item to be retrieved
     * @return the item retrieved represented as a string
     */
    public String removeItem(int itemNumber) {
        Session session = getSession();

        Item item = (Item) session.createCriteria(Item.class)
                        .add(Restrictions.eq("listNumber", itemNumber)).uniqueResult();
        if (item == null) {
            return null;
        }
        
        session.delete(item);
        int listNumberDeleted = item.getListNumber();
        updateNumbers(session, listNumberDeleted);
        
        endSession(session);
        return item.toString();
    }
    
    /**
     * Remove an item from the database
     *
     * @param itemKeyword  the keyword to find the item
     * @return the item removed
     */
    public String removeItem(String itemKeyword) {
        ArrayList<Item> items = getAllItems();
        itemKeyword = itemKeyword.toLowerCase();

        if (items.isEmpty()) {
            return null;
        }

        for (int n = 0; n < items.size(); n++) {
            String itemLowerCase = items.get(n).toString().toLowerCase();
            if (itemLowerCase.contains(itemKeyword)) {
                Session session = getSession();
                
                Item item = items.get(n);
                session.delete(item);
                int listNumberDeleted = item.getListNumber();
                updateNumbers(session, listNumberDeleted);

                endSession(session);
                return items.get(n).toString();
            }
        }
        return null;
    }

    /**
     * Update database values in list_number
     *
     * @param session  the current session
     * @param listNumberDeleted  the list_number deleted from the database
     */
    public void updateNumbers(Session session, int listNumberDeleted) {
        String hql = "UPDATE " + Item.class.getName() + " SET list_number = list_number -1 WHERE list_number > "
                        + listNumberDeleted;
        Query query = session.createQuery(hql);
        query.executeUpdate();
    }

    /**
     * Get all items from database
     * 
     * @return items  a list of items from database
     */
    private ArrayList<Item> getAllItems() {
        Session session = getSession();

        ArrayList<Item> items = new ArrayList<Item>();

        for (int n = 1; n <= getItemCount(); n++) {
            Item item = (Item) session.createCriteria(Item.class)
                        .add(Restrictions.eq("listNumber", n)).uniqueResult();
            if (item != null) {
                items.add(item);
            }
        }

        endSession(session);
        return items;
    }
    
    /**
     * Get all text from database
     * 
     * @return items  a list of text from database
     */
    public ArrayList<String> getAllText() {
        Session session = getSession();

        ArrayList<String> text = new ArrayList<String>();

        for (int n = 1; n <= getItemCount(); n++) {
            Item item = (Item) session.createCriteria(Item.class)
                        .add(Restrictions.eq("listNumber", n)).uniqueResult();
            if (item != null) {
                text.add(item.toString());
            }
        }

        endSession(session);
        return text;
    }

    /**
     * Get all the items with the keyword.
     *
     * @param itemKeyword  the keyword to find the items
     * @return itemsWithKeyword  a list of items with the keyword
     */
    public ArrayList<String> getAllItemsWithKeyword(String itemKeyword) {
        ArrayList<String> allText = getAllText();
        ArrayList<String> itemsWithKeyword = new ArrayList<String>();
        itemKeyword = itemKeyword.toLowerCase();

        for (int n = 0; n < allText.size(); n++) {
            if (allText.get(n).toLowerCase().contains(itemKeyword)) {
                itemsWithKeyword.add(allText.get(n));
            }
        }
        return itemsWithKeyword;
    }

    /**
     * Get the item based on the item number
     *
     * @param itemNumber  the number of the item to be retrieved
     * @return the item with the given item number
     */
    public String getItem(int itemNumber) {
        return getItem(itemNumber + "),");
    }

    /**
     * Get the item based on a keyword.
     * If there are two or more items with the keyword, the first in the list will be returned.
     *
     * @param itemKeyword  a keyword to find the items
     * @return the item containing the keyword
     */
    public String getItem(String itemKeyword) {
        ArrayList<String> text = getAllText();
        itemKeyword = itemKeyword.toLowerCase();

        for (int n = 0; n < text.size(); n++) {
            if (text.get(n).toLowerCase().contains(itemKeyword)) {
                return text.get(n);
            }
        }
        return null;
    }
}
