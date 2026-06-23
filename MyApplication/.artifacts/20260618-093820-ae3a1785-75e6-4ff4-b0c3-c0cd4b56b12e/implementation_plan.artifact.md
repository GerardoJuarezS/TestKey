# Implementation Plan - PDF Saving and Dark Theme Fixes

This plan outlines the changes to allow users to save PDF quotes to a chosen location and fix several UI/UX issues where elements remain white in Dark Theme.

## Proposed Changes

### PDF Saving Functionality

I will implement a "Save PDF" button alongside the "Share" button. This will use the Storage Access Framework (SAF) to let the user choose a save location.

#### [QuoteListScreen.kt](file:///C:/Users/gerar/OneDrive/AndroidStudioProjects/MyApplication/app/src/main/java/com/example/myapplication/ui/QuoteListScreen.kt)

- Add `onSavePdf` callback to `EliteQuoteCard`.
- Add a "SAVE" button in `EliteQuoteCard` next to "SHARE".
- Implement `rememberLauncherForActivityResult` with `CreateDocument` to handle PDF saving.

#### [QuoteViewModel.kt](file:///C:/Users/gerar/OneDrive/AndroidStudioProjects/MyApplication/app/src/main/java/com/example/myapplication/ui/QuoteViewModel.kt)

- Add a helper method to copy the generated PDF file to a given `Uri`.

---

### UI/UX Dark Theme Fixes

I will audit and fix components that use hardcoded colors or `Color.White` instead of theme-aware colors.

#### [QuoteListScreen.kt](file:///C:/Users/gerar/OneDrive/AndroidStudioProjects/MyApplication/app/src/main/java/com/example/myapplication/ui/QuoteListScreen.kt)

- Ensure `EliteQuoteCard` backgrounds and text use `MaterialTheme.colorScheme`.
- Fix `SearchBar` colors.

#### [QuoteEditScreen.kt](file:///C:/Users/gerar/OneDrive/AndroidStudioProjects/MyApplication/app/src/main/java/com/example/myapplication/ui/QuoteEditScreen.kt)

- Update `HtmlInputField` and `EliteCard` to use proper surface colors.
- Ensure dialogs (`LocationPickerDialog`, etc.) use the correct theme colors.
- Fix hardcoded `Color.White` in `LocationPickerDialog` search results.

#### [InventoryScreen.kt](file:///C:/Users/gerar/OneDrive/AndroidStudioProjects/MyApplication/app/src/main/java/com/example/myapplication/ui/InventoryScreen.kt)

- Update `EliteInventoryCard` and dialogs (`MaterialDialog`, `ServiceDialog`) to use `MaterialTheme.colorScheme`.

#### [AboutScreen.kt](file:///C:/Users/gerar/OneDrive/AndroidStudioProjects/MyApplication/app/src/main/java/com/example/myapplication/ui/AboutScreen.kt)

- Replace hardcoded `SoftBg` with `MaterialTheme.colorScheme.background`.
- Update `Card` colors and hardcoded `Color.White`.

---

### PDF Styling Improvement

The PDF is currently hardcoded with a white background and specific colors. I'll ensure it remains professional and readable.

#### [PdfGenerator.kt](file:///C:/Users/gerar/OneDrive/AndroidStudioProjects/MyApplication/app/src/main/java/com/example/myapplication/util/PdfGenerator.kt)

- No changes planned here as the PDF *should* have a white background for printing, but I'll double-check readability.

## Verification Plan

### Automated Tests
- No automated tests are available in this project for UI.

### Manual Verification
1.  **PDF Saving**: Open the app, go to the quotes list, and click the new "SAVE" button on a quote. Verify that it opens the file picker and saves the file correctly.
2.  **Dark Theme Audit**:
    - Switch to Dark Theme.
    - Check Quote List (Search bar, Cards).
    - Check Edit Quote Screen (All input fields, Map dialog, Summary card).
    - Check Catalog Screen (Tabs, Cards, Dialogs).
    - Check About Screen.
    - Verify no "blinding white" areas remain.
