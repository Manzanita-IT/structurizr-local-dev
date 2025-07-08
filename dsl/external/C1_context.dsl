workspace extends ../C0_landscape.dsl {

    name "External"
    description "Example of an External System"

    model {
    }

    views {
        systemContext external "SystemContext" {
            include *
            autolayout tb
        }
    }

    configuration {
        scope softwaresystem
    }

}
