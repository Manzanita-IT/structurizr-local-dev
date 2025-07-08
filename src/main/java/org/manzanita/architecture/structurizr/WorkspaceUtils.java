package org.manzanita.architecture.structurizr;

import com.structurizr.Workspace;
import com.structurizr.model.Element;
import com.structurizr.model.Model;
import com.structurizr.model.Person;
import com.structurizr.model.Relationship;
import com.structurizr.model.SoftwareSystem;
import com.structurizr.model.StaticStructureElement;
import java.util.Optional;
import javax.annotation.Nonnull;
import lombok.SneakyThrows;

class WorkspaceUtils {

    static boolean isPersonOrSystem(Element element) {
        return element instanceof Person || element instanceof SoftwareSystem;
    }

    static void cloneNonExistingRelationship(Relationship relationship, Model model) {
        cloneNonExistingRelationship(relationship, model, "");
    }

    static void cloneNonExistingRelationship(Relationship relationship, Model model, String suffix) {
        Optional<Relationship> clonedRelationship = Optional.empty();

        if (relationship.getSource() instanceof SoftwareSystem
                && relationship.getDestination() instanceof SoftwareSystem) {
            SoftwareSystem source = model.getSoftwareSystemWithName(
                    relationship.getSource().getName());
            SoftwareSystem destination = model.getSoftwareSystemWithName(
                    relationship.getDestination().getName());

            clonedRelationship = cloneRelation(relationship, source, destination, suffix);
        } else if (relationship.getSource() instanceof Person
                && relationship.getDestination() instanceof SoftwareSystem) {
            Person source = model.getPersonWithName(relationship.getSource().getName());
            SoftwareSystem destination = model.getSoftwareSystemWithName(
                    relationship.getDestination().getName());

            clonedRelationship = cloneRelation(relationship, source, destination, suffix);
        } else if (relationship.getSource() instanceof SoftwareSystem
                && relationship.getDestination() instanceof Person) {
            SoftwareSystem source = model.getSoftwareSystemWithName(
                    relationship.getSource().getName());
            Person destination = model.getPersonWithName(relationship.getDestination().getName());

            clonedRelationship = cloneRelation(relationship, source, destination, suffix);
        } else if (relationship.getSource() instanceof Person
                && relationship.getDestination() instanceof Person) {
            Person source = model.getPersonWithName(relationship.getSource().getName());
            Person destination = model.getPersonWithName(relationship.getDestination().getName());

            clonedRelationship = cloneRelation(relationship, source, destination, suffix);
        }

        clonedRelationship.ifPresent(cr -> cr.addTags(relationship.getTags()));
    }

    private static Optional<Relationship> cloneRelation(Relationship relationship,
            StaticStructureElement source, StaticStructureElement destination,
            @Nonnull String suffix) {
        if (source != null && destination != null && !source.hasEfferentRelationshipWith(
                destination)) {
            return Optional.ofNullable(
                    source.uses(destination, relationship.getDescription() + suffix, null, null));
        }
        return Optional.empty();
    }

    static String urlTo(Workspace workspace) {
        return "{workspace:" + workspace.getId() + "}/diagrams#SystemContext";
    }

    @SneakyThrows
    static Workspace fromJson(String json) {
        return com.structurizr.util.WorkspaceUtils.fromJson(json);
    }

}
