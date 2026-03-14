package org.pracainzynierska.sportbooking

import org.koin.dsl.module

val sharedModule = module {
    single { SportApi() }
}
