workspace extends C1_context.dsl {

    !identifiers hierarchical

    model {
        !element internal {
            application = container "Service" {
                technology "Java/Spring Boot"
                -> external "Makes calls to" "REST"
            }
        }
    }

    views {
        container internal "Containers" {
            include *
            autolayout tb
        }
    }

}
