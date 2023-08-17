package org.example;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfig;
import org.eclipse.milo.opcua.sdk.client.api.identity.UsernameProvider;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaMonitoredItem;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaSubscription;
import org.eclipse.milo.opcua.sdk.client.model.nodes.objects.ServerTypeNode;
import org.eclipse.milo.opcua.sdk.client.model.nodes.variables.ServerStatusTypeNode;
import org.eclipse.milo.opcua.sdk.client.nodes.UaNode;
import org.eclipse.milo.opcua.sdk.client.nodes.UaVariableNode;
import org.eclipse.milo.opcua.sdk.client.subscriptions.OpcUaSubscription;
import org.eclipse.milo.opcua.stack.client.DiscoveryClient;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.*;
import org.eclipse.milo.opcua.stack.core.types.enumerated.ServerState;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.eclipse.milo.opcua.stack.core.types.structured.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Main {
    public static void main(String[] args) throws Exception {
        NodeId nodeId = new NodeId(2, 123);
        OpcUaClient a = verbinden();
        node_auslesen(a,nodeId);
        //Variant b = daten_lesen(a, nodeId);
        //daten_schreiben(a, nodeId);
        //Variant d = daten_lesen(a, nodeId);
        //System.out.println(d);


    }

    public static OpcUaClient verbinden() throws Exception {
        String endpointUrl = "opc.tcp://milo.digitalpetri.com:62541/milo"; // Replace with your endpoint URL

        List<EndpointDescription> endpoints = DiscoveryClient.getEndpoints(endpointUrl).get();

        EndpointDescription endpoint = endpoints.stream()
                // .filter(e -> e.getSecurityPolicyUri().equals(SecurityPolicy.None.getSecurityPolicyUri()))
                .findFirst().orElseThrow(() -> new Exception("No suitable endpoint found"));

        OpcUaClientConfig config = OpcUaClientConfig.builder()
                .setEndpoint(endpoint)
                .setApplicationName(LocalizedText.english("OPC UA Client Example"))
                .setIdentityProvider(new UsernameProvider("user1", "password"))
                .build();

        OpcUaClient client = OpcUaClient.create(config);
        client.connect().get();
        return client;
    }

    public static Variant daten_lesen(OpcUaClient client, NodeId nodeId) throws ExecutionException, InterruptedException, UaException {


        ReadValueId readValueId = new ReadValueId(
                nodeId,    // Node ID of the variable
                AttributeId.Value.uid(),  // Attribute ID for "Value"
                null,      // IndexRange (null for all)
                QualifiedName.NULL_VALUE // Data Encoding (null for default)
        );

        UaVariableNode variableNode = client.getAddressSpace().getVariableNode(nodeId);
        DataValue dataValue = variableNode.readValue();

        Variant value = dataValue.getValue();
        System.out.println("Received value: " + value.getValue());
        return value;
    }

    public static void daten_schreiben(OpcUaClient client, NodeId nodeId) throws ExecutionException, InterruptedException {
        Variant newValue = new Variant(8999); // Replace with the value you want to write
        CompletableFuture<StatusCode> writeFuture = client.writeValue(nodeId, new DataValue(newValue));
        writeFuture.get();
    }

    /*public static void daten_abonnieren(OpcUaClient client, NodeId nodeId) throws ExecutionException, InterruptedException {
        OpcUaSubscription subscription = client.getSubscriptionManager().createSubscription(1000.0).get();

        ReadValueId readValueId = new ReadValueId(
                nodeId,
                AttributeId.Value.uid(),
                null,
                QualifiedName.NULL_VALUE
        );

        MonitoringParameters parameters = new MonitoringParameters(
                10.0, // Beispielabtastrate in Millisekunden
                null, // Filter
                null, // QueueSize
                10,   // DiscardOldest
                true  // Aktivieren Sie die Überwachung direkt nach dem Erstellen
        );

        UaMonitoredItem monitoredItem = subscription.createMonitoredItem(
                readValueId,
                TimestampsToReturn.Server,
                parameters
        ).get();

        // Warte eine Weile und beende dann das Abonnement
        Thread.sleep(10000);
        subscription.deleteMonitoredItem(monitoredItem.getMonitoredItemId());
        subscription.delete();
        client.disconnect().get();
    }
    }
     */
    public static void node_auslesen(OpcUaClient client, NodeId nodeId) throws ExecutionException, InterruptedException, UaException {

        // CompletableFuture<List<ReferenceDescription>> nodesFuture = (CompletableFuture<List<ReferenceDescription>>) client.getAddressSpace().browse(rootId);
        //final Logger logger = LoggerFactory.getLogger(Main.class);
    /*
        nodesFuture.thenApply(nodes -> {
            for ( ReferenceDescription node : nodes) {
                ExpandedNodeId nodeId = node.getNodeId();
                System.out.println("NodeID: " + nodeId);
            }
            return null;
        });
        */

        // Get a typed reference to the Server object: ServerNode
        ServerTypeNode serverNode = (ServerTypeNode) client.getAddressSpace().getObjectNode(
                Identifiers.Server,
                Identifiers.ServerType
        );
        UaNode node = client.getAddressSpace().getNode(nodeId);
        List <ReferenceDescription> browser = node.browse();
        //System.out.println(node.browse());
        List<Node> subNodes = new ArrayList<>();
        for (ReferenceDescription nodelist :browser) {
            System.out.println("Node IDEltern: " + nodelist.getNodeId());

            // Hinzufügen des untergeordneten Knotens zur Liste
            // subNodes.add(client.getAddressSpace().getNode(childNodeId));
        }

        // Iteration durch die Liste der untergeordneten Knoten
        for (Node subNode : subNodes) {
            System.out.println("Node ID: " + subNode.getNodeId());
            System.out.println("Browse Name: " + subNode.getBrowseName());
            // Weitere Informationen über den untergeordneten Knoten...
        }
        // Read properties of the Server object...
        String[] serverArray = serverNode.getServerArray();
        String[] namespaceArray = serverNode.getNamespaceArray();

        System.out.println(Arrays.toString(serverArray));
        System.out.println(Arrays.toString(namespaceArray));

        // Read the value of attribute the ServerStatus variable component
        ServerStatusDataType serverStatus = serverNode.getServerStatus();

        System.out.println(serverStatus);

        // Get a typed reference to the ServerStatus variable
        // component and read value attributes individually
        ServerStatusTypeNode serverStatusNode = serverNode.getServerStatusNode();
        BuildInfo buildInfo = serverStatusNode.getBuildInfo();
        DateTime startTime = serverStatusNode.getStartTime();
        DateTime currentTime = serverStatusNode.getCurrentTime();
        ServerState state = serverStatusNode.getState();

        System.out.println( buildInfo);
        //logger.info("ServerStatus.StartTime={}", startTime);
        //logger.info("ServerStatus.CurrentTime={}", currentTime);
        System.out.println(state);
    }
}
