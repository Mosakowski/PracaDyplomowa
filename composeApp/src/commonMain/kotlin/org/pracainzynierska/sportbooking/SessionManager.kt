package org.pracainzynierska.sportbooking

import androidx.compose.runtime.mutableStateOf
import org.pracainzynierska.sportbooking.viewmodels.*
import org.koin.dsl.module
import org.koin.core.module.dsl.viewModel
import org.koin.core.parameter.ParametersHolder

class SessionManager {
    var currentUser = mutableStateOf<AuthResponse?>(null)
}

val appModule = module {
    single { SessionManager() }
    
    viewModel { FacilitiesViewModel(api = get()) }
    viewModel { LoginViewModel(api = get()) }
    viewModel { RegisterViewModel(api = get()) }
    viewModel { MyBookingsViewModel(api = get(), sessionManager = get()) }
    
    viewModel { params -> 
        SchedulerViewModel(
            api = get(), 
            sessionManager = get(), 
            facility = params.get<FacilityDto>()
        ) 
    }
    
    viewModel { params -> 
        FacilityDetailsViewModel(
            facility = params.get<FacilityDto>()
        ) 
    }
    
    viewModel { AdminViewModel(api = get(), sessionManager = get()) }
    viewModel { OwnerDashboardViewModel(api = get(), sessionManager = get()) }
    
    viewModel { params -> 
        FacilityManagerViewModel(
            api = get(), 
            sessionManager = get(), 
            facility = params.get<FacilityDto>()
        ) 
    }
    
    viewModel { params -> 
        OwnerFacilityDetailsViewModel(
            api = get(), 
            sessionManager = get(), 
            initialFacility = params.get<FacilityDto>()
        ) 
    }
}
