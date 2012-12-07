package project.cs.lisa.application.http.test;

import java.util.HashSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;

import project.cs.lisa.application.http.Locator;
import project.cs.lisa.application.http.NetInfPublish;
import project.cs.lisa.application.http.NetInfPublishResponse;
import project.cs.lisa.application.http.NetInfResponse;
import project.cs.lisa.application.http.NetInfStatus;
import project.cs.lisa.mock.MockServer;
import project.cs.lisa.util.metadata.Metadata;
import android.test.InstrumentationTestCase;

public class NetInfPublishTest extends InstrumentationTestCase {

    public static final String TAG = "NetInfPublishTest";

    private static final int TIMEOUT_SECONDS = 5;

    private static final String mHash = "hashThatExists";

    private static final String mHost = "localhost";
    private static final String mHashAlg = "sha-256";
    private static final String mBluetoothMac = "11:22:33:44:55:66";
    private static final String mMetaKey = "metaKey";
    private static final String mMetaValue = "metaValue";
    private static final String mMetaJsonTag = "meta";

    private MockServer mMockServer;

    public static class MockServerResource extends ServerResource {
        @Put
        public void mockGet() {

            assertNotNull("Hash not in URI query", getQuery().getFirstValue("hash"));
            assertNotNull("Hash algorithm not in URI query", getQuery().getFirstValue("hashAlg"));

            String jsonMeta = getQuery().getFirstValue("meta");
            if (jsonMeta != null) {
                Object extObj = JSONValue.parse(jsonMeta);
                assertTrue("Ext should be JSON", extObj instanceof JSONObject);
                JSONObject ext = (JSONObject) extObj;
                assertTrue("Ext should have contained a specific key", ext.containsKey(mMetaJsonTag));
                Object metaObj = ext.get(mMetaJsonTag);
                assertTrue("Meta should be JSON", metaObj instanceof JSONObject);
                JSONObject meta = (JSONObject) metaObj;
                assertTrue("Meta should have contained a specific key", meta.containsKey(mMetaKey));
                assertEquals("Meta contained wrong data for the specific key", mMetaValue, meta.get(mMetaKey));
            }

        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // Setup HTTP Server
        mMockServer = new MockServer();
        mMockServer.attach("/publish", MockServerResource.class);
        assertNotNull("Failed to create mock server", mMockServer);
        mMockServer.start();
        assertTrue("Server should be started", mMockServer.isStarted());

    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        // Shutdown server
        mMockServer.stop();
        assertTrue("Server should be stopped", mMockServer.isStopped());
    }

    public void testMockServerSetup() {
    }

    public void testPublishWithoutLocator() throws Throwable {

        // Signal used to wait for ASyncTask
        final CountDownLatch signal = new CountDownLatch(1);

        HashSet<Locator> locators = null;

        final NetInfPublish publish = new NetInfPublish(mHost, Integer.toString(MockServer.PORT), mHashAlg, mHash, locators) {
            @Override
            protected void onPostExecute(NetInfResponse response) {
                assertNotNull("Should always receive a response", response);
                assertTrue("Response should be a publish response", response instanceof NetInfPublishResponse);
                assertFalse("Publish should not have succeeded",
                        ((NetInfPublishResponse) response).getStatus() == NetInfStatus.OK);

                // Signal done
                signal.countDown();
            }
        };

        // Run on UI thread
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                publish.execute();
            }
        });

        // Wait a few seconds for the done signal, if timeout fail
        assertTrue("Request took more than " + TIMEOUT_SECONDS + " seconds.",
                signal.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));

    }

    public void testPublishOneLocator() throws Throwable {

        // Signal used to wait for ASyncTask
        final CountDownLatch signal = new CountDownLatch(1);

        HashSet<Locator> locators = new HashSet<Locator>();
        Locator locator = new Locator(Locator.Type.BLUETOOTH, mBluetoothMac);
        locators.add(locator);

        final NetInfPublish publish = new NetInfPublish(mHost, Integer.toString(MockServer.PORT), mHashAlg, mHash, locators) {
            @Override
            protected void onPostExecute(NetInfResponse response) {
                assertNotNull("Should always receive a response", response);
                assertTrue("Response should be a publish response", response instanceof NetInfPublishResponse);
                assertEquals("Publish should have been executed successfully",
                        NetInfStatus.OK, ((NetInfPublishResponse) response).getStatus());

                // Signal done
                signal.countDown();
            }
        };

        // Run on UI thread
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                publish.execute();
            }
        });

        // Wait a few seconds for the done signal, if timeout fail
        assertTrue("Request took more than " + TIMEOUT_SECONDS + " seconds.",
                signal.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));

    }

    public void testPublishMultipleLocators() throws Throwable {

        // Signal used to wait for ASyncTask
        final CountDownLatch signal = new CountDownLatch(1);

        HashSet<Locator> locators = new HashSet<Locator>();
        Locator locator1 = new Locator(Locator.Type.BLUETOOTH, mBluetoothMac);
        Locator locator2 = new Locator(Locator.Type.BLUETOOTH, mBluetoothMac);
        locators.add(locator1);
        locators.add(locator2);

        final NetInfPublish publish = new NetInfPublish(mHost, Integer.toString(MockServer.PORT), mHashAlg, mHash, locators) {
            @Override
            protected void onPostExecute(NetInfResponse response) {
                assertNotNull("Should always receive a response", response);
                assertTrue("Response should be a publish response", response instanceof NetInfPublishResponse);
                assertEquals("Publish should have been executed successfully",
                        NetInfStatus.OK, ((NetInfPublishResponse) response).getStatus());

                // Signal done
                signal.countDown();
            }
        };

        // Run on UI thread
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                publish.execute();
            }
        });

        // Wait a few seconds for the done signal, if timeout fail
        assertTrue("Request took more than " + TIMEOUT_SECONDS + " seconds.",
                signal.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));

    }

    public void testPublishWrongPort() throws Throwable {

        // Signal used to wait for ASyncTask
        final CountDownLatch signal = new CountDownLatch(1);

        // Locator
        HashSet<Locator> locators = new HashSet<Locator>();
        Locator locator = new Locator(Locator.Type.BLUETOOTH, mBluetoothMac);
        locators.add(locator);

        final NetInfPublish publish = new NetInfPublish(mHost, Integer.toString(MockServer.WRONG_PORT), mHashAlg, mHash, locators) {
            @Override
            protected void onPostExecute(NetInfResponse response) {
                assertNotNull("Should always receive a response", response);
                assertTrue("Response should be a publish response", response instanceof NetInfPublishResponse);
                assertFalse("Publish should not have succeeded",
                        ((NetInfPublishResponse) response).getStatus() == NetInfStatus.OK);
                // Signal done
                signal.countDown();
            }
        };

        // Run on UI thread
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                publish.execute();
            }
        });

        // Wait a few seconds for the done signal, if timeout fail
        assertTrue("Request took more than " + TIMEOUT_SECONDS + " seconds.",
                signal.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));

    }

    public void testPublishWithMetadata() throws Throwable {

        // Signal used to wait for ASyncTask
        final CountDownLatch signal = new CountDownLatch(1);

        // Locators
        HashSet<Locator> locators = new HashSet<Locator>();
        Locator locator = new Locator(Locator.Type.BLUETOOTH, mBluetoothMac);
        locators.add(locator);

        final NetInfPublish publish = new NetInfPublish(mHost, Integer.toString(MockServer.PORT), mHashAlg, mHash, locators) {
            @Override
            protected void onPostExecute(NetInfResponse response) {
                assertNotNull("Should always receive a response", response);
                assertTrue("Response should be a publish response", response instanceof NetInfPublishResponse);
                assertEquals("Publish should have been executed successfully",
                        NetInfStatus.OK, ((NetInfPublishResponse) response).getStatus());
                // Signal done
                signal.countDown();
            }
        };

        // Metadata
        Metadata metadata = new Metadata();
        metadata.insert(mMetaKey, mMetaValue);

        publish.setMetadata(metadata);
        
        // Run on UI thread
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                publish.execute();
            }
        });

        // Wait a few seconds for the done signal, if timeout fail
        assertTrue("Request took more than " + TIMEOUT_SECONDS + " seconds.",
                signal.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));

    }

}
