package mf.asciitext;

import android.content.Context;
import android.content.Intent;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputConnection;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import mf.asciitext.fonts.AppFont;
import mf.asciitext.fonts.AvailableFonts;


public class MyInputMethodService extends InputMethodService implements KeyboardView.OnKeyboardActionListener {

    private final int ALPHA_KEYBOARD_KEYCODE = -10;
    private final int SECONDARY_KBD_KEYCODE = -11;
    private final int ALPHA_KBD = 0;
    private final int NUMBER_KBD = 1;
    private final int MATH_KBD = 2;
    private final int REGULAR_FONT_INDEX = -1;


    private KeyboardView keyboardView;
    private Keyboard keyboard;
    private int keyboardChoice = ALPHA_KBD;
    private AppCompatImageView fontStyleToggle;

    final private List<AppFont> fonts = AvailableFonts.INSTANCE.getFonts();
    private int fontIndex = REGULAR_FONT_INDEX;
    private int lastSelectedStyleIndex = REGULAR_FONT_INDEX;

    RecyclerView fontPicker;
    GridLayoutManager layoutManager;
    FontPickerAdapter adapter;

    @Override
    public View onCreateInputView() {
        final View layout = getLayoutInflater().inflate(R.layout.keyboard_view, null);
        final Context ctx = layout.getContext();

        keyboardView = layout.findViewById(R.id.keyboard_view);
        keyboard = new Keyboard(this, R.xml.keyboard);
        keyboardView.setKeyboard(keyboard);
        keyboardView.setOnKeyboardActionListener(this);

        fontPicker = layout.findViewById(R.id.fontPicker);
        adapter = new FontPickerAdapter(fonts, onFontSelection());
        layoutManager = new GridLayoutManager(ctx, 1, LinearLayoutManager.HORIZONTAL, false);
        fontPicker.setLayoutManager(layoutManager);
        fontPicker.setAdapter(adapter);
        adapter.setSelectedFont(fontIndex);

        fontStyleToggle = layout.findViewById(R.id.font_button);
        fontStyleToggle.setOnClickListener(onFontButtonClick());
        setFontStyleIcon(fontIndex == REGULAR_FONT_INDEX);

        layout.findViewById(R.id.settings_button).setOnClickListener(onSettingsClick(ctx));

        return layout;
    }

    @Override
    public void onPress(int i) {
    }

    @Override
    public void onRelease(int i) {

    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        InputConnection inputConnection = getCurrentInputConnection();

        if (inputConnection != null) {
            switch (primaryCode) {
                case SECONDARY_KBD_KEYCODE:
                    if (keyboardChoice == NUMBER_KBD) {
                        keyboard = new Keyboard(this, R.xml.keyboard_math);
                        keyboardChoice = MATH_KBD;
                    } else {
                        keyboard = new Keyboard(this, R.xml.keyboard_extended);
                        keyboardChoice = NUMBER_KBD;
                    }
                    keyboardView.setKeyboard(keyboard);
                    keyboardView.invalidateAllKeys();
                    break;
                case ALPHA_KEYBOARD_KEYCODE:
                    keyboard = new Keyboard(this, R.xml.keyboard);
                    keyboardChoice = ALPHA_KBD;
                    setShiftKeyIcon();
                    keyboardView.setKeyboard(keyboard);
                    keyboardView.invalidateAllKeys();
                    break;
                case Keyboard.KEYCODE_DELETE:
                    CharSequence selectedText = inputConnection.getSelectedText(0);
                    if (TextUtils.isEmpty(selectedText)) {
                        inputConnection.deleteSurroundingText(1, 0);
                    } else {
                        inputConnection.commitText("", 1);
                    }
                    break;
                case Keyboard.KEYCODE_SHIFT:
                    keyboard.setShifted(!keyboard.isShifted());
                    setShiftKeyIcon();
                    keyboardView.invalidateAllKeys();
                    break;
                case Keyboard.KEYCODE_DONE:
                    inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                    break;
                default:
                    char code = (char) primaryCode;
                    if (Character.isLetter(code) && keyboard.isShifted()) {
                        code = Character.toUpperCase(code);
                    }
                    String text = String.valueOf(code);

                    if (fontIndex >= 0 && fontIndex < fonts.size()) {
                        AppFont style = fonts.get(fontIndex);
                        text = style.encode(text);
                    }

                    inputConnection.commitText(text, 1);
            }
        }
    }

    @Override
    public void onText(CharSequence charSequence) {

    }

    @Override
    public void swipeLeft() {

    }

    @Override
    public void swipeRight() {

    }

    @Override
    public void swipeDown() {

    }

    @Override
    public void swipeUp() {

    }

    private FontPickerAdapter.OnItemClickListener onFontSelection() {
        return new FontPickerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(AppFont item, int index) {
                int previous = lastSelectedStyleIndex;
                fontIndex = index;
                lastSelectedStyleIndex = index;
                setFontStyleIcon(false);
                adapter.setSelectedFont(fontIndex);
                adapter.notifyItemChanged(fontIndex);
                adapter.notifyItemChanged(previous);
            }
        };
    }

    private View.OnClickListener onSettingsClick(final Context context) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, SettingsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        };
    }

    private View.OnClickListener onFontButtonClick() {

        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean disable = fontIndex != REGULAR_FONT_INDEX;
                fontIndex = disable ? REGULAR_FONT_INDEX : lastSelectedStyleIndex;
                setFontStyleIcon(disable);
                adapter.setSelectedFont(fontIndex);
            }
        };
    }

    private void setFontStyleIcon(boolean disable) {
        fontStyleToggle.setImageResource(disable ?
                R.drawable.ic_font_off : R.drawable.ic_format_font);
        fontPicker.setAlpha(disable ? 0.5f : 1.0f);
    }

    private void setShiftKeyIcon() {
        List<Keyboard.Key> keys = keyboard.getKeys();

        for (int n = 0; n < keys.size() - 1; n++) {
            Keyboard.Key currentKey = keys.get(n);
            if (currentKey.codes[0] == Keyboard.KEYCODE_SHIFT) {
                currentKey.icon = getResources().getDrawable(
                        keyboard.isShifted() ?
                                R.drawable.ic_keyboard_caps_filled :
                                R.drawable.ic_arrow_up_bold_outline
                );
                break;
            }
        }
    }
}
