import SwiftUI
import shared

struct SummarySheet: View {

    @Binding var isPresented: Bool

    ///

    @State private var vm = SummarySheetVM()

    var body: some View {

        VMView(vm: vm, stack: .VStack()) { state in

            Spacer()

            Sheet__BottomView {

                VStack {

                    HStack {

                        ForEachIndexed(state.periodHints) { _, period in

                            Button(
                                    action: {
                                        vm.setPeriod(
                                                pickerTimeStart: period.pickerTimeStart,
                                                pickerTimeFinish: period.pickerTimeFinish
                                        )
                                    },
                                    label: {
                                        Text(period.title)
                                                .font(.system(size: 13, weight: period.isActive ? .bold : .light))
                                                .foregroundColor(period.isActive ? c.white : c.text)
                                                .padding(.horizontal, 8)
                                                .padding(.vertical, 6)
                                    }
                            )
                        }
                    }
                            .padding(.top, 8)
                }
            }
        }
    }
}
