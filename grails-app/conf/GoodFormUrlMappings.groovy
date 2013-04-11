class GoodFormUrlMappings {

    static mappings = {

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
    }
}
