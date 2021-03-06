/*
 *                               This program is free software: you can redistribute it and/or modify
 *                                it under the terms of the GNU General Public License as published by
 *                                the Free Software Foundation, version 3 of the License.
 *
 *                                This program is distributed in the hope that it will be useful,
 *                                but WITHOUT ANY WARRANTY; without even the implied warranty of
 *                                MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *                                GNU General Public License for more details.
 *
 *                                You should have received a copy of the GNU General Public License
 *                                along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *                                Copyright (c) 2019 Colin Redman
 */

package io.mongodb;

import org.debatetool.core.*;
import org.debatetool.io.initializers.DatabaseInitializer;
import org.debatetool.io.iocontrollers.IOController;
import org.debatetool.io.iocontrollers.mongodb.MongoDBIOController;
import org.debatetool.io.structureio.StructureIOManager;
import org.junit.AfterClass;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

class MongoDBStructureIOManagerTest {
    Card card;
    Card card2;
    Block block;
    public final String ANALYTIC_TEXT = "This is an analytic";

    @BeforeClass
    public void setUp(){
        IOController.setIoController(new MongoDBIOController());
        try {
            IOController.getIoController().attemptInitialize(new DatabaseInitializer("127.0.0.1", 27017, null,null));
        } catch (IOException e) {
            e.printStackTrace();
        }
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("testCardText.txt").getFile());
        String text = null;
        try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(file))){
            text = new String(in.readAllBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        card = new Card(new Cite("Smith", "2010", "Renowned writer of cards"), text);
        card2 = new Card(new Cite("Smith", "2010", "Renowned writer of cards"), text+"AAA");

        block = new Block("Test Block");
        block.addComponent(card);
        block.addComponent(card2);
        block.addComponent(new Analytic(ANALYTIC_TEXT));
        try {
            IOController.getIoController().getComponentIOManager().storeSpeechComponent(card);
            IOController.getIoController().getComponentIOManager().storeSpeechComponent(card2);
            IOController.getIoController().getComponentIOManager().storeSpeechComponent(block);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void directoriesAddGetTest (){
        try {
            StructureIOManager manager = IOController.getIoController().getStructureIOManager();
            ArrayList<String> emptyList = new ArrayList<>();
            manager.addChild(emptyList, "test_dir");

            ArrayList<String> testDirPath = new ArrayList<>();
            testDirPath.add("test_dir");
            manager.addContent(testDirPath, card);
            manager.addContent(testDirPath, card2);

            ArrayList<String> testChildDirPath = new ArrayList<>();
            testChildDirPath.add("test_dir");
            testChildDirPath.add("test_child_dir");
            manager.addChild(testDirPath, "test_child_dir");
            manager.addContent(testChildDirPath, card2);
            manager.addContent(testChildDirPath, block);

            Assert.assertEquals(manager.getChildren(emptyList), testDirPath);
            Assert.assertTrue(manager.getContent(testDirPath).contains(card));
            Assert.assertTrue(manager.getContent(testDirPath).contains(card2));
            Assert.assertTrue(manager.getContent(testChildDirPath).contains(card2));
            List<HashIdentifiedSpeechComponent> contents = manager.getContent(testChildDirPath);
            for (HashIdentifiedSpeechComponent component:contents){
                component.load();
            }
            Assert.assertTrue(contents.contains(block));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    public void tearDown(){
        try {
            IOController.getIoController().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}