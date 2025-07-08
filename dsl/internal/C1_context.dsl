workspace extends ../C0_landscape.dsl {

    name "Internal"
    description "Example of an Internal System"

    model {
        administrator -> internal "Administers"
        user -> internal "Uses"
        internal -> external "Uses"
    }

    views {
        systemContext internal "SystemContext" {
            include *
            autolayout tb
        }
    }

    configuration {
        scope softwaresystem
    }

}
