workspace {

  !identifiers hierarchical

  model {
    !include _landscape_/systems.dsl
    !include _landscape_/persona.dsl
    // No relationships here
  }

  views {
    styles {
      !include _landscape_/styles.dsl
    }
  }

}
