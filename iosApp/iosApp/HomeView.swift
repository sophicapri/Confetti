import SwiftUI
import ConfettiKit

struct HomeView: View {
    private let component: HomeComponent
    
    @StateValue
    private var stack: ChildStack<AnyObject, HomeComponentChild>
    
    init(_ component: HomeComponent) {
        self.component = component
        _stack = StateValue(component.stack)
    }
    
    var body: some View {
        VStack {
            let child = stack.active.instance
            
            ChildView(child: child)
                .frame(maxHeight: .infinity)
            
            HStack(alignment: .bottom, spacing: 16) {
                BottomTabView(
                    title: "Schedule",
                    systemImage: "calendar",
                    isActive: child is HomeComponentChild.Sessions,
                    action: component.onSessionsTabClicked
                )
                
                BottomTabView(
                    title: "Speakers",
                    systemImage: "person",
                    isActive: child is HomeComponentChild.Speakers,
                    action: component.onSpeakersTabClicked
                )
                
                BottomTabView(
                    title: "Bookmarks",
                    systemImage: "bookmark",
                    isActive: child is HomeComponentChild.Bookmarks,
                    action: component.onBookmarksTabClicked
                )
                
                BottomTabView(
                    title: "Venue",
                    systemImage: "location",
                    isActive: child is HomeComponentChild.Venue,
                    action: component.onVenueTabClicked
                )

            }
        }
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                Button("Switch", action: component.onSwitchConferenceClicked)
            }
        }
    }
}

private struct ChildView: View {
    let child: HomeComponentChild
    
    var body: some View {
        switch child {
        case let child as HomeComponentChild.Sessions: SessionsView(child.component)
        case let child as HomeComponentChild.MultiPane: MultiPaneView(child.component)
        case let child as HomeComponentChild.Speakers: SpeakersView(child.component)
        case let child as HomeComponentChild.Bookmarks: BookmarksView(child.component)
        case let child as HomeComponentChild.Venue: VenueView(child.component)
        default: EmptyView()
        }
    }
}

private struct BottomTabView: View {
    let title: String
    let systemImage: String
    let isActive: Bool
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            Label(title, systemImage: systemImage)
                .labelStyle(VerticalLabelStyle())
                .opacity(isActive ? 1 : 0.5)
        }
    }
}

private struct VerticalLabelStyle: LabelStyle {
    func makeBody(configuration: Configuration) -> some View {
        VStack(alignment: .center, spacing: 8) {
            configuration.icon
            configuration.title
        }
    }
}
