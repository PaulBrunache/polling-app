package faskteam.faskandroid.utilities;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;

import faskteam.faskandroid.R;


/**
 * Class that handles alert messages to the user..
 * DisplayAlert* methods create and show alert dialogues to the user with the
 * desired message.
 * DisplayToast* methods create and show Toast notifications to the user
 * with the desired message.
 * TODO future implementation may include push notification functionalities...or that may be done
 * to a separate class.
 */
public class DisplayAlert {

    /***
     * Displays an error message to the user in the form of an alert Dialog
     * @param context Context of the current activity
     * @param msg Message to be sent to user
     */
    public static void displayAlertError(Context context, String msg, String title) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle( (title != null && title.length() > 0) ? title : "Oops." );

        builder.setMessage(msg)
                .setPositiveButton(context.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        builder.create().show();
    }

    public static void displaySimpleMessage(Context context, String  msg, String title) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        if (title != null && title.length() > 0) {
            builder.setTitle(title);
        }
        builder.setMessage(msg)
                .setPositiveButton(context.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        builder.create().show();
    }

    public static AlertDialog displayCustomMessage(Context context, String  msg, @Nullable String title, AlertDetails details, @Nullable final AlertCallback call) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        if (title != null && title.length() > 0) {
            builder.setTitle(title);
        }
        builder.setMessage(msg)
                .setPositiveButton(details.positiveButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (call != null) {
                            call.onPositive(dialog);
                            call.onAny(dialog);
                        }
                    }
                }).setNegativeButton(details.negativeButton, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (call != null) {
                    call.onNegative(dialog);
                    call.onAny(dialog);
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
        return dialog;
    }

    public static class AlertDetails {
        private String positiveButton;
        private String negativeButton;

        public AlertDetails(String positiveButton, String negativeButton) {
            this.positiveButton = positiveButton;
            this.negativeButton = negativeButton;
        }

    }

    public interface AlertCallback {
        void onPositive(DialogInterface dialog);

        void onNegative(DialogInterface dialog);

        void onAny(DialogInterface dialog);
    }

    //TODO create toast messages
}
