import SwiftUI
import WatchConnectivity
import Combine
import shared

///
/// Kmm

let DI = shared.DI.shared

func time() -> Int {
    UtilsKt.time().toInt()
}

func reportApi(_ message: String) {
    UtilsKt.reportApi(message: message)
}

func zlog(_ message: Any?) {
    UtilsKt.zlog(message: message)
}

//////

let roundedShape = RoundedRectangle(cornerRadius: 99, style: .circular)
let squircleShape = RoundedRectangle(cornerRadius: 12, style: .continuous)

/// Based on https://stackoverflow.com/a/26962452/5169420
func machineIdentifier() -> String {
    var systemInfo = utsname()
    uname(&systemInfo)
    let machineMirror = Mirror(reflecting: systemInfo.machine)
    return machineMirror.children.reduce("") { identifier, element in
        guard let value = element.value as? Int8, value != 0 else { return identifier }
        return identifier + String(UnicodeScalar(UInt8(value)))
    }
}

func ForEachIndexedId<T, Content: View>(
        _ items: Array<T>,
        @ViewBuilder content: @escaping (Int, T) -> Content
) -> some View {
    ForEach(Array(items.enumerated()), id: \.offset) { index, item in
        content(index, item)
    }
}

/// Watch Connectivity
func setupWCSession(_ delegate: WCSessionDelegate) {
    if WCSession.isSupported() {
        let session = WCSession.default
        session.delegate = delegate
        session.activate()
    } else {
        zlog("setupWCSession is not supported")
    }
}

///
///

func myAsync(_ function: @escaping () -> Void) {
    DispatchQueue.main.async {
        function()
    }
}

func myAsyncAfter(
        _ seconds: CGFloat,
        work: @escaping () -> Void
) {
    DispatchQueue.main.asyncAfter(deadline: .now() + seconds) {
        work()
    }
}

//////

// todo move to KMM

func is12HoursFormat() -> Bool {
    DateFormatter.dateFormat(fromTemplate: "j", options: 0, locale: Locale.current)?.range(of: "a") != nil
}

///
/// Custom HStack/VStack/Spacer for default spacing

struct HStack<Content: View>: View {

    var alignment: VerticalAlignment = .center
    var spacing: CGFloat? = 0
    @ViewBuilder let content: () -> Content

    var body: some View {
        SwiftUI.HStack(alignment: alignment, spacing: spacing, content: content)
    }
}

struct VStack<Content: View>: View {

    var alignment: HorizontalAlignment = .center
    var spacing: CGFloat? = 0
    @ViewBuilder let content: () -> Content

    var body: some View {
        SwiftUI.VStack(alignment: alignment, spacing: spacing, content: content)
    }
}

struct Spacer: View {

    var body: some View {
        SwiftUI.Spacer(minLength: 0)
    }
}

//////

struct Padding: View {

    var horizontal: Double = 0
    var vertical: Double = 0

    var body: some View {
        ZStack {}
                .frame(width: horizontal)
                .frame(height: vertical)
    }
}

//////

extension Date {

    /// For device time zone
    func startOfDay() -> Date {
        Calendar.current.startOfDay(for: self)
    }

    func inSeconds(_ seconds: Int) -> Date {
        var components = DateComponents()
        components.second = seconds
        return Calendar.current.date(
                byAdding: components,
                to: self
        )!
    }

    func toUnixTime() -> UnixTime {
        UnixTime(time: Int32(timeIntervalSince1970), utcOffset: UtilsKt.localUtcOffset)
    }
}

extension UnixTime {

    func toDate() -> Date {
        Date(timeIntervalSince1970: Double(time))
    }
}

extension Array {

    // todo remove
    func chunked(
            _ size: Int
    ) -> [[Element]] {
        stride(from: 0, to: count, by: size).map {
            Array(self[$0..<Swift.min($0 + size, count)])
        }
    }
}

extension FixedWidthInteger {

    func toInt() -> Int {
        Int(self)
    }

    func toInt32() -> Int32 {
        Int32(self)
    }

    func toDouble() -> Double {
        Double(self)
    }

    func toKotlinInt() -> KotlinInt {
        KotlinInt(integerLiteral: Int(self))
    }

    func toString() -> String {
        String(self)
    }

    ///

    func asUnixDayToDate() -> Date {
        Date(timeIntervalSince1970: Double(UnixTime.companion.byLocalDay(localDay: Int32(self), utcOffset: UtilsKt.localUtcOffset).time))
    }

    func asTimeToDate() -> Date {
        Date(timeIntervalSince1970: Double(self))
    }
}

extension Int {

    func limitMin(_ value: Int) -> Int {
        self < value ? value : self
    }

    func limitMax(_ value: Int) -> Int {
        self > value ? value : self
    }

    func limitMinMax(_ min: Int, _ max: Int) -> Int {
        limitMin(min).limitMax(max)
    }
}

extension KotlinInt {

    func toInt() -> Int {
        Int(self)
    }
}

extension Double {

    func limitMin(_ value: Double) -> Double {
        self < value ? value : self
    }

    func limitMax(_ value: Double) -> Double {
        self > value ? value : self
    }

    func limitMinMax(_ min: Double, _ max: Double) -> Double {
        limitMin(min).limitMax(max)
    }

    //////

    func goldenRatioUp() -> Double {
        self * Double(UtilsKt.GOLDEN_RATIO)
    }

    func goldenRatioDown() -> Double {
        self / Double(UtilsKt.GOLDEN_RATIO)
    }
}

///
/// Exceptions

extension Error {

    func myMessage() -> String {
        if let error = self as? MyError {
            return error.message
        }
        return localizedDescription
    }
}

class MyError: Error {

    let message: String

    init(_ message: String) {
        self.message = message
    }
}

//////

extension Array {

    /// https://stackoverflow.com/a/25330930
    func getOrNull(index: Int) -> Element? {
        if index < 0 {
            fatalError()
        }
        return index < count ? self[index] : nil
    }
}

extension UIColor {

    convenience init(argb: UInt) {
        self.init(
                red: Double((argb >> 16) & 0xff) / 255,
                green: Double((argb >> 8) & 0xff) / 255,
                blue: Double((argb >> 0) & 0xff) / 255,
                alpha: Double((argb >> 24) & 0xff) / 255
        )
    }
}

extension Color {

    init(rgba: [Int]) {
        self.init(
                .sRGB,
                red: Double(rgba[0]) / 255,
                green: Double(rgba[1]) / 255,
                blue: Double(rgba[2]) / 255,
                opacity: Double(rgba.getOrNull(index: 3) ?? 255) / 255
        )
    }

    init(rgbaString: String) {
        self.init(rgba: rgbaString.split(separator: ",").map {
            Int($0)!
        })
    }
}

extension ColorNative {

    func toColor() -> Color {
        switch self {
        case .red:
            return .red
        case .green:
            return .green
        case .blue:
            return .blue
        case .orange:
            return .orange
        case .purple:
            return .purple
        case .white:
            return .white
        case .black:
            return .black
        case .text:
            return .primary
        case .textsecondary:
            return .secondary
        case .transparent:
            return .clear
        case .timertitledefault:
            #if os(iOS)
            return Color(UIColor.myDayNight(UIColor(.blue), .white))
            #else
            return .white
            #endif
        default:
            fatalError("ColorNative.toColor()")
        }
    }
}

extension View {

    /**
     * https://www.avanderlee.com/swiftui/conditional-view-modifier/
     */
    @ViewBuilder func conditional<Content: View>(
            _ condition: Bool,
            transform: (Self) -> Content
    ) -> some View {
        if condition { transform(self) } else { self }
    }

    /**
     * https://stackoverflow.com/a/72435691
     */
    @ViewBuilder func cornerRadius(
            _ radius: CGFloat,
            onTop: Bool,
            onBottom: Bool
    ) -> some View {
        self
                .conditional(onTop) { view in
                    view
                            .padding(.bottom, radius)
                            .cornerRadius(radius)
                            .padding(.bottom, -radius)
                }
                .conditional(onBottom) { view in
                    view
                            .padding(.top, radius)
                            .cornerRadius(radius)
                            .padding(.top, -radius)
                }
    }
}

///
///

private struct AnimateVmValueModifier<T: Equatable>: ViewModifier {

    let value: T
    @Binding var state: T
    let animation: Animation

    func body(content: Content) -> some View {
        content
                .onChange(of: value) { newValue in
                    withAnimation(animation) {
                        state = newValue
                    }
                }
                .onAppear {
                    state = value
                }
    }
}

extension View {

    func animateVmValue<T: Equatable>(
            value: T,
            state: Binding<T>,
            animation: Animation = .spring(response: 0.250)
    ) -> some View {
        modifier(AnimateVmValueModifier(value: value, state: state, animation: animation))
    }
}

///
/// https://medium.com/@michael.forrest.music/how-to-make-a-scrollview-or-list-in-swiftui-that-starts-from-the-bottom-b0c4a69beb0d

struct FlippedUpsideDown: ViewModifier {
    func body(content: Content) -> some View {
        content
                .rotationEffect(.radians(.pi))
                .scaleEffect(x: -1, y: 1, anchor: .center)
    }
}

extension View {
    func flippedUpsideDown() -> some View {
        self.modifier(FlippedUpsideDown())
    }
}

/// https://stackoverflow.com/a/62588295/5169420
/// Made for scroll calculation for RepeatingFormSheet
struct ViewOffsetKey: PreferenceKey {

    typealias Value = CGFloat
    static var defaultValue = CGFloat.zero

    static func reduce(value: inout Value, nextValue: () -> Value) {
        value += nextValue()
    }
}

///
///

extension Kotlinx_coroutines_coreFlow {

    func toPublisher<T: AnyObject>() -> AnyPublisher<T, Never> {
        let swiftFlow = SwiftFlow<T>(kotlinFlow: self)
        return Deferred<Publishers.HandleEvents<PassthroughSubject<T, Never>>> {
            let subject = PassthroughSubject<T, Never>()
            let cancelable = swiftFlow.watch { next in
                if let next = next {
                    subject.send(next)
                }
            }
            return subject.handleEvents(receiveCancel: {
                cancelable.cancel()
            })
        }
                .eraseToAnyPublisher()
    }
}

///
///

struct VMView<VMState: AnyObject, Content: View>: View {

    private let vm: __VM<VMState>
    @State private var state: VMState
    private let publisher: AnyPublisher<VMState, Never>
    @ViewBuilder private let content: (VMState) -> Content
    private let stack: StackType

    init(
            vm: __VM<VMState>,
            stack: StackType = .ZStack(),
            @ViewBuilder content: @escaping (VMState) -> Content
    ) {
        self.vm = vm
        state = vm.state.value as! VMState
        publisher = vm.state.toPublisher()
        self.stack = stack
        self.content = content
    }

    var body: some View {
        ZStack {
            switch stack {
            case .ZStack(let p1):
                ZStack(alignment: p1) { content(state) }
            case .VStack(let p1, let p2):
                VStack(alignment: p1, spacing: p2) { content(state) }
            case .HStack(let p1, let p2):
                HStack(alignment: p1, spacing: p2) { content(state) }
            }
        }
                /// In onAppear() because init() is called frequently even the
                /// view is not showed. "Unnecessary" calls is 90% times.
                /// Yes, onAppear() calls too late but the default values DI saves.
                .onAppear {
                    vm.onAppear()
                }
                .onDisappear {
                    vm.onDisappear()
                }
                //////
                .onReceive(publisher) { res in
                    state = res
                }
    }

    enum StackType {
        case ZStack(alignment: Alignment = .center)
        case VStack(alignment: HorizontalAlignment = .center, spacing: CGFloat? = 0)
        case HStack(alignment: VerticalAlignment = .center, spacing: CGFloat? = 0)
    }
}
