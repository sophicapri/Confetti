import SwiftUI
import ConfettiKit

struct BookmarksView: View {
    private let component: BookmarksComponent
    
    @StateValue
    private var uiState: BookmarksComponentUiState
    
    
    init(_ component: BookmarksComponent) {
        self.component = component
        _uiState = StateValue(component.uiState)
    }
    
    var body: some View {
        
        VStack {
            switch uiState {
            case is BookmarksComponentLoading: ProgressView()
            case is BookmarksComponentError: ErrorView()
            case let state as BookmarksComponentSuccess:
                BookmarksContentView(component: component,
                                     bookmarks: state.bookmarks,
                                     upcomingSessions: state.upcomingSessions as! Dictionary<LocalDateTime, Array<SessionDetails>>,
                                     pastSessions: state.pastSessions as! Dictionary<LocalDateTime, Array<SessionDetails>>)
            default: EmptyView()
            }
        }
        .navigationBarTitle("Bookmarks", displayMode: .inline)
        .listStyle(.insetGrouped)
    }
}


private struct BookmarksContentView: View {
    let component: BookmarksComponent
    let bookmarks: Set<String>
    let upcomingSessions: Dictionary<LocalDateTime, Array<SessionDetails>>
    let pastSessions: Dictionary<LocalDateTime, Array<SessionDetails>>
    let sessionsPerTab: Array<Dictionary<LocalDateTime, Array<SessionDetails>>>
    
    @State private var selectedTabIndex: Int = 0
    
    init(component: BookmarksComponent, bookmarks: Set<String>, upcomingSessions: Dictionary<LocalDateTime, Array<SessionDetails>>, pastSessions: Dictionary<LocalDateTime, Array<SessionDetails>>) {
        self.component = component
        self.bookmarks = bookmarks
        self.upcomingSessions = upcomingSessions
        self.pastSessions = pastSessions
        sessionsPerTab = [pastSessions, upcomingSessions]
    }
    
    
    var body: some View {
        VStack {
            Picker(selection: $selectedTabIndex, label: Text("Date")) {
                Text("Past").tag(0)
                Text("Upcoming").tag(1)
            }
            .pickerStyle(.segmented)
            .padding([.leading, .trailing], 16)
            .padding(.bottom, 8)
            
            List {
                
                ForEach(Array(sessionsPerTab[selectedTabIndex].keys), id: \.self) { date in
                    Section(header: HStack {
                        Image(systemName: "clock")
                        Text("\(date)").font(.headline).bold()
                    }) {
                        
                        let sessions = sessionsPerTab[selectedTabIndex][date] ?? []
                        ForEach(sessions, id: \.self) { session in
                            SessionView(session: session)
                                .frame(maxWidth: .infinity, alignment: .leading)
                                .contentShape(Rectangle())
                                .onTapGesture { component.onSessionClicked(id: session.id) }
                                .listRowBackground(selectedTabIndex == 0 ? Color(.systemFill) : Color(uiColor: .systemBackground))
                        }
                    }
                }
            }
        }
    }
}

/*
private struct dictionary {
    var dic = [LocalDateTime() : [SessionDetails(id: "13",
                            title: "Session",
                            type: "type",
                            startsAt: Kotlinx_datetimeLocalDateTime(
                                year: 23, monthNumber: 12, dayOfMonth: 12, hour: 12, minute: 34, second: Int32(34), nanosecond: Int32(34)
                            ),
                            endsAt: Kotlinx_datetimeLocalDateTime(
                                year: 23, monthNumber: 12, dayOfMonth: 12, hour: 12, minute: 34, second: Int32(34), nanosecond: Int32(34)
                            ),
                            sessionDescription: nil,
                            language: nil,
                            speakers: [],
                            room: nil,
                            tags: [],
                            __typename: "typename"
                           )]
    ]
}
*/
