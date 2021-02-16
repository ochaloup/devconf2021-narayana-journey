package io.narayana;

import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.Services;
import org.omg.CORBA.TRANSACTION_ROLLEDBACK;
import org.omg.CosTransactions.Control;
import org.omg.CosTransactions.Resource;
import org.omg.CosTransactions.TransactionFactory;
import org.omg.CosTransactions.TransactionFactoryHelper;

import java.util.Properties;

public class JTSMain implements AutoCloseable {

    /**
     * This has to be changed to the IP of the client, which would be accessible from Docker container
     */
    private static String CLIENT_IP = System.getProperty("CLIENT_IP", "localhost");

    /**
     * This has to be changed to the IP of the JTS Docker image
     */
    private static String NAME_SERVER_IP = System.getProperty("NAME_SERVER_IP", "localhost");

    /**
     * This has to be changed to the PORT of the name service running inside JTS Docker image.
     */
    private static String NAME_SERVER_PORT = System.getProperty("NAME_SERVER_PORT", "3528");

    private ORB testORB;
    private OA testOA;
    private TransactionFactory transactionFactory;

    public void setup() {
        /**
         * Initialise ORB
         */
        final Properties orbProperties = new Properties();
        orbProperties.setProperty("ORBInitRef.NameService", "corbaloc::" + NAME_SERVER_IP + ":" + NAME_SERVER_PORT
                + "/StandardNS/NameServer-POA/_root");
        orbProperties.setProperty("OAIAddr", CLIENT_IP);

        testORB = ORB.getInstance("test");
        testOA = OA.getRootOA(testORB);

        try {
            testORB.initORB(new String[]{}, orbProperties);
            testOA.initOA();

            ORBManager.setORB(testORB);
            ORBManager.setPOA(testOA);

            /**
             * Initialise transaction factory
             */
            final Services services = new Services(testORB);
            final int resolver = Services.getResolver();
            final String[] serviceParameters = new String[]{Services.otsKind};

            org.omg.CORBA.Object service = services.getService(Services.transactionService, serviceParameters, resolver);
            transactionFactory = TransactionFactoryHelper.narrow(service);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot initialize ORB for " + NAME_SERVER_IP + ":" + NAME_SERVER_PORT
                    + ", clientIP: " + CLIENT_IP, e);
        }
    }

    @Override
    public void close() {
        if (testOA != null) testOA.destroy();
        if (testORB != null) testORB.shutdown();
    }

    public static void main(String[] args) {
        // JTSMain jtsMain = new JTSMain();
        try (JTSMain jtsMain = new JTSMain()) {
            jtsMain.setup();
            jtsMain.testCommit();
        }
    }

    public void testCommit() {
        System.out.println("JTSDockerContainerTest.testCommit");
        System.out.println("Begin transaction");
        Control control = transactionFactory.create(0);

        final TestResource firstResource = new TestResource(true);
        final TestResource secondResource = new TestResource(true);

        final Resource firstReference = firstResource.getReference();
        final Resource secondReference = secondResource.getReference();

        try {
            System.out.println("Enlist resources");
            control.get_coordinator().register_resource(firstReference);
            control.get_coordinator().register_resource(secondReference);

            System.out.println("Commit transaction");
            control.get_terminator().commit(true);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot enlist and/or commit ORB transaction.", e);
        }
    }

    public void testRollback() throws Exception {
        System.out.println("JTSDockerContainerTest.testRollback");
        System.out.println("Begin transaction");
        Control control = transactionFactory.create(0);

        final TestResource firstResource = new TestResource(true);
        final TestResource secondResource = new TestResource(true);

        final Resource firstReference = firstResource.getReference();
        final Resource secondReference = secondResource.getReference();

        System.out.println("Enlist resources");
        control.get_coordinator().register_resource(firstReference);
        control.get_coordinator().register_resource(secondReference);

        System.out.println("Rollback transaction");
        control.get_terminator().rollback();
    }

    public void testResourceRollback() throws Exception {
        System.out.println("JTSDockerContainerTest.testResourceRollback");
        System.out.println("Begin transaction");
        Control control = transactionFactory.create(0);

        final TestResource firstResource = new TestResource(true);
        final TestResource secondResource = new TestResource(false);

        final Resource firstReference = firstResource.getReference();
        final Resource secondReference = secondResource.getReference();

        System.out.println("Enlist resources");
        control.get_coordinator().register_resource(firstReference);
        control.get_coordinator().register_resource(secondReference);

        System.out.println("Commit transaction");

        try {
            control.get_terminator().commit(true);
            throw new IllegalStateException("Rollback expected, commit cannot work.");
        } catch (TRANSACTION_ROLLEDBACK e) {
            // Expected
        }
    }

}
