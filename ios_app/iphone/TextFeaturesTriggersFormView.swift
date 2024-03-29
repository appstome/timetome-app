import SwiftUI
import shared

struct TextFeaturesTriggersFormView: View {

    private let formUI: TextFeaturesTriggersFormUI
    private let onChange: (TextFeatures) -> Void

    @State private var isChecklistsPickerPresented = false
    @State private var isShortcutsPickerPresented = false

    init(
            textFeatures: TextFeatures,
            onChange: @escaping (TextFeatures) -> Void
    ) {
        self.onChange = onChange
        formUI = TextFeaturesTriggersFormUI(textFeatures: textFeatures)
    }

    var body: some View {

        VStack {

            MyListView__ItemView(
                    isFirst: true,
                    isLast: false
            ) {

                MyListView__ItemView__ButtonView(
                        text: formUI.checklistsTitle,
                        withArrow: true,
                        rightView: AnyView(
                                MyListView__ItemView__ButtonView__RightText(
                                        text: formUI.checklistsNote,
                                        paddingEnd: 2
                                )
                        )
                ) {
                    isChecklistsPickerPresented = true
                }
            }
                    .sheetEnv(isPresented: $isChecklistsPickerPresented) {
                        ChecklistsPickerSheet(
                                isPresented: $isChecklistsPickerPresented,
                                selectedChecklists: formUI.textFeatures.checklists
                        ) { checklists in
                            onChange(formUI.setChecklists(checklists: checklists))
                        }
                    }

            MyListView__ItemView(
                    isFirst: false,
                    isLast: true,
                    withTopDivider: true
            ) {

                MyListView__ItemView__ButtonView(
                        text: formUI.shortcutsTitle,
                        withArrow: true,
                        rightView: AnyView(
                                MyListView__ItemView__ButtonView__RightText(
                                        text: formUI.shortcutsNote,
                                        paddingEnd: 2
                                )
                        )
                ) {
                    isShortcutsPickerPresented = true
                }
            }
                    .sheetEnv(isPresented: $isShortcutsPickerPresented) {
                        ShortcutsPickerSheet(
                                isPresented: $isShortcutsPickerPresented,
                                selectedShortcuts: formUI.textFeatures.shortcuts
                        ) { shortcut in
                            onChange(formUI.setShortcuts(shortcuts: shortcut))
                        }
                    }
        }
    }
}
