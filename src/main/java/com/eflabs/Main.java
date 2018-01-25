package com.eflabs;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphException;
import org.janusgraph.core.JanusGraphFactory;

public class Main {

    public static final String PROP_ID = "_id";
    public static final String VERTEX_LABEL = "vertex_type";
    public static final String EDGE_LABEL = "edge_type";

    public static void main(String[] args) throws Exception {

        JanusGraphFactory.Builder builder = JanusGraphFactory
                .build()
                .set("storage.backend", "inmemory")
                .set("ids.flush", true);

        JanusGraph janusGraph = builder.open();

        try {
            GraphTraversalSource traversal = janusGraph.traversal();

            Vertex v1 = traversal.addV(VERTEX_LABEL).tryNext().get();
            v1.property(PROP_ID, "1");

            Vertex v2 = traversal.addV(VERTEX_LABEL).tryNext().get();
            v2.property(PROP_ID, "2");

            v1.addEdge(EDGE_LABEL, v2);

            traversal.tx().commit();
            traversal.tx().rollback();

            Edge e = traversal.V().has(PROP_ID, "1").outE(EDGE_LABEL).tryNext().get();
            assert e != null;

            traversal.tx().rollback();

            e = traversal.V().has(PROP_ID, "1")
                    .outE(EDGE_LABEL)
                    .inV().has(PROP_ID, "2")
                    .inE(EDGE_LABEL)
                    .tryNext()
                    .get();

            e.property("prop1", "update");

            // IllegalArgumentException thrown from VertexIDAssigner#assignID line 300 when ids.flush=false
            traversal.tx().commit();

        } catch (JanusGraphException e) {
            e.printStackTrace();

        } finally {
            janusGraph.close();

        }
    }

}
