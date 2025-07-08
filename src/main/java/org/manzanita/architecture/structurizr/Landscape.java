package org.manzanita.architecture.structurizr;

import static lombok.AccessLevel.PRIVATE;

import com.structurizr.Workspace;
import com.structurizr.model.Model;
import com.structurizr.view.ViewSet;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = PRIVATE)
public class Landscape {

    private final Workspace workspace;

    static Landscape from(Workspace workspace) {
        return new Landscape(workspace);
    }

    public Model getModel() {
        return workspace.getModel();
    }

    public ViewSet getViews() {
        return workspace.getViews();
    }

}
