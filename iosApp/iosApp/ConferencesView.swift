import SwiftUI
import ConfettiKit

struct ConferencesView: View {
    private let component: ConferencesComponent
    
    @StateValue
    private var uiState: ConferencesComponentUiState
    
    init(_ component: ConferencesComponent) {
        self.component = component
        _uiState = StateValue(component.uiState)
    }
    
    var body: some View {
        return NavigationView {
            VStack  {
                switch uiState {
                case let uiState as ConferencesComponentSuccess:
                    ConferencesByYearView(component: component, conferencesUiState: uiState)
                case is ConferencesComponentError: ErrorView()
                default: ProgressView()
                }
            }.navigationBarTitle("Confetti", displayMode: .inline)
        }
    }
}

private struct ConferencesByYearView: View {
    let component: ConferencesComponent
    let conferencesUiState: ConferencesComponentSuccess

    var body: some View {
        VStack {
            let conferencesByYear = conferencesUiState.conferenceListByYear

            List {
                ForEach(Array(conferencesByYear.keys).sorted { $0.intValue > $1.intValue }, id: \.self) { year in
                    
                    Section(header: HStack {
                        Text(year.stringValue).font(.headline).bold()
                    }) {
                        let conferences = conferencesUiState.conferenceListByYear[year] ?? []
                        ForEach(conferences, id: \.self) { conference in
                            HStack {
                                Text(conference.name)
                                Spacer()
                                Text("\(conference.days[0])")
                            }
                            .background(
                                Rectangle()
                                .foregroundColor(.clear)
                                .contentShape(Rectangle())
                            )
                            .onTapGesture {
                                component.onConferenceClicked(conference: conference)
                            }
                        }
                    }
                }
            }
        }
    }
}
