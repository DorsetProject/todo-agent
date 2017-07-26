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

import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import edu.jhuapl.dorset.Application;
import edu.jhuapl.dorset.Request;
import edu.jhuapl.dorset.Response;
import edu.jhuapl.dorset.agents.Agent;
import edu.jhuapl.dorset.routing.Router;
import edu.jhuapl.dorset.routing.SingleAgentRouter;

public class ToDoListAgentTest {
    
    public Application setUpAgent() {
        Config config = ConfigFactory.load();
        Agent agent = new ToDoListAgent(config);
        Router router = new SingleAgentRouter(agent);
        Application app = new Application(router);
        return app;
    }
    
    public Response makeRequest(Application app, String text) {
        Request request = new Request(text);
        return app.process(request);
    }
    
    public void setOrCleanUp(Application app, String text) {
        Request request = new Request(text);
        app.process(request);
    }
    
    @Test
    public void testAdd() {
        Application app = setUpAgent();

        Response response = makeRequest(app, "ADD this is an item");

        assertTrue(response.getText().contains("Item added: "));

        setOrCleanUp(app, "REMOVE is an item");
    }

    @Test
    public void testRemoveByNumGood() {
        Application app = setUpAgent();

        Response response = makeRequest(app, "REMOVE 1");

        assertTrue(response.getText().contains("Item removed: "));

        setOrCleanUp(app, "ADD " + response.getText().substring(response.getText().indexOf("M,") + 2));
    }

    @Test
    public void testRemoveByNumBad() {
        Application app = setUpAgent();

        Response response = makeRequest(app, "REMOVE 20");

        assertTrue(response.getStatus().getMessage().contains("Error:"));
    }

    @Test
    public void testRemoveByKeywordGood() {
        Application app = setUpAgent();

        setOrCleanUp(app, "ADD Buy notebook");

        Response response = makeRequest(app, "REMOVE notebook");

        assertTrue(response.getText().contains("Item removed: "));
    }

    @Test
    public void testRemoveByKeywordBad() {
        Application app = setUpAgent();

        Response response = makeRequest(app, "REMOVE non-existent item");

        assertTrue(response.getStatus().getMessage().contains("Error:"));
    }

    @Test
    public void testGetAllText() {
        Application app = setUpAgent();

        Response response = makeRequest(app, "GET ALL");

        assertTrue(response.getText().contains("TODO List"));
    }

    @Test
    public void testGetAllItemsWithKeywordGood() {
        Application app = setUpAgent();

        setOrCleanUp(app, "ADD Buy desk organizers");
        
        Response response = makeRequest(app, "GET ALL Buy");

        setOrCleanUp(app, "REMOVE Buy desk organizers");

        assertTrue(response.getText().contains("Buy desk organizers"));
    }

    @Test
    public void testGetAllItemsWithKeywordBad() {
        Application app = setUpAgent();

        Response response = makeRequest(app, "GET ALL non-existent");

        assertTrue(response.getStatus().getMessage().contains("Error:"));
    }

    @Test
    public void testGetAllItemsWithDateGood() {
        Application app = setUpAgent();

        setOrCleanUp(app, "ADD Today's new item");

        Response response = makeRequest(app, "GET ALL " + new SimpleDateFormat("MM/dd/yyyy").format(new Date()));

        setOrCleanUp(app, "REMOVE Today's new item");

        assertTrue(response.getText().contains("Today's new item"));
    }

    @Test
    public void testGetAllItemsWithDateBad() {
        Application app = setUpAgent();

        Response response = makeRequest(app, "GET ALL 08/30/20");

        assertTrue(response.getStatus().getMessage().contains("Error:"));
    }

    @Test
    public void testGetItemByNumGood() {
        Application app = setUpAgent();

        Response response = makeRequest(app, "GET 1");

        assertTrue(response.getText().contains("1),"));
    }

    @Test
    public void testGetItemByNumBad() {
        Application app = setUpAgent();

        Response response = makeRequest(app, "GET 20");

        assertTrue(response.getStatus().getMessage().contains("Error:"));
    }

    @Test
    public void testGetItemByKeywordGood() {
        Application app = setUpAgent();
        
        setOrCleanUp(app, "ADD Buy a new item");
        
        Response response = makeRequest(app, "GET new item");
        System.out.println(response.getStatus().getMessage());
        setOrCleanUp(app, "REMOVE a new item");

        assertTrue(response.getText().contains("Buy a new item"));
    }

    @Test
    public void testGetItemByKeywordBad() {
        Application app = setUpAgent();

        Response response = makeRequest(app, "GET none");

        assertTrue(response.getStatus().getMessage().contains("Error:"));
    }
}
