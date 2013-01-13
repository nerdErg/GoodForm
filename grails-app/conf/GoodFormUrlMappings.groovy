class GoodFormUrlMappings {

    static mappings = {
        "/suggest/$subject"(controller: 'suggest', action: 'suggest')
        "/$controller/view/$id/$name?"(action: 'view') {
            constraints {
                controller(matches: /.*Form/)
            }
        }
        "/$controller/back/$id/$qset?"(action: 'back') {
            constraints {
                controller(matches: /.*Form/)
            }
        }
        "/$controller/$action?/$id?" {
            constraints {
                // apply constraints here
            }
        }

        "/"(view: "/index")
        "500"(view: '/error')
    }
}
