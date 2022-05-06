package de.benjaminaaron.ontoserver.model.graph;

import org.jgrapht.graph.AbstractBaseGraph;
import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.util.SupplierUtil;

public class DirectedMultigraphWithSelfLoops <V, E> extends AbstractBaseGraph<V, E> {
    public DirectedMultigraphWithSelfLoops(Class<? extends E> edgeClass) {
        super(null, SupplierUtil.createSupplier(edgeClass),
                (new DefaultGraphType.Builder()).directed().allowMultipleEdges(true).allowSelfLoops(true).weighted(false).build());
    }
}
