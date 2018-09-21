package io.github.alexescalonafernandez.filesplit.task.write;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by alexander.escalona on 21/09/2018.
 */
public interface WriteTask extends Runnable {

    /**
     * Release the resources and close the file writer
     * @param os the writer to close
     */
    default void closeOutputStream(OutputStream os)  {
        try {
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
