package client;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/// <summary>
/// Wraps a socket and provides the ability to read line-by-line in a
/// binary safe manner, as needed by the HTTP protocol.
/// </summary>
public class Connection
{
    // These three buffering fields are only used for reading. The class does not buffer writes.
    private int readOffset;
    private int writeOffset;
    private byte[] buffer;
    private Socket socket;

    int SpareCapacity() {
        return buffer.length - writeOffset;
    }

    public Lines Request;

    public Connection(Socket socket, int readBufferSize)
    {
        this.socket = socket;
        buffer = new byte[readBufferSize];
        readOffset = writeOffset = 0;
    }

    public void Write(String text) throws IOException {
        Write(text.getBytes(StandardCharsets.US_ASCII));
    }

    public void Write(byte[] buffer) throws IOException {
        socket.getOutputStream().write(buffer);
    }

    /// <summary>
    /// Read the specified number of bytes from the connection.
    /// </summary>
    public byte[] ReadBytes(int count) throws IOException, EndOfStreamException {
        var result = new byte[count];
        int resultIndex = 0;
        while (resultIndex < count)
        {
            int bytesToCopy = writeOffset - readOffset;
            if (bytesToCopy == 0)
            {
                FeedBuffer();
                bytesToCopy = writeOffset - readOffset;
            }
            if (resultIndex + bytesToCopy > count)
                bytesToCopy = count - resultIndex;
            System.arraycopy(buffer, readOffset, result, resultIndex, bytesToCopy);
            resultIndex += bytesToCopy;
            readOffset += bytesToCopy;
        }
        return result;
    }

    /// <summary>
    /// Read a single CRLF-terminated ASCII line from the socket.
    /// Throws if line ending is missing or invalid, or if line length
    /// exceeds the configured read buffer size.
    /// </summary>
    public String readLine(boolean requireCrLf) throws IOException, EndOfStreamException {
        int searchFrom = readOffset;
        while (true)
        {
            for (; searchFrom < writeOffset && buffer[searchFrom] != '\n'; searchFrom++) {}

            if (searchFrom != buffer.length && buffer[searchFrom] == '\n') break;

            searchFrom = writeOffset; // On next iteration, only search the newly read data
            FeedBuffer();
        }

        int nextLineOffset = searchFrom + 1; // skip the LF
        if (searchFrom - 1 < readOffset || buffer[searchFrom - 1] != (byte)'\r')
        {
            if (requireCrLf)
                throw new IOException("Connection received LF terminated line (HTTP lines must be CR LF terminated)");
        }
        else
        {
            --searchFrom; // do not include the final CR in the line
        }

        var line = new String(buffer, readOffset, searchFrom - readOffset, StandardCharsets.US_ASCII);
        readOffset = nextLineOffset;
        return line;
    }

    /// <summary>
    /// Read the request body as an UTF-8 string. Request must specify Content-Length.
    /// </summary>
    public String ReadRequestBody() throws Lines.KeyNotFoundException, IOException, EndOfStreamException {
        var contentLength = Integer.parseInt(Request.get("Content-Length").trim());
        return new String(ReadBytes(contentLength), StandardCharsets.UTF_8);
    }

    /// <summary>
    /// Moves buffered data to a new offset within buffer (no error checking).
    /// </summary>
    void MoveBufferedDataToOffset() {
        int bufferedDataSize = writeOffset - readOffset;
        System.arraycopy(buffer, readOffset, buffer, 0, bufferedDataSize);
        writeOffset = bufferedDataSize;
        readOffset = 0;
    }

    /// <summary>
    /// Read more data from socket into the read buffer.
    /// </summary>
    void FeedBuffer() throws IOException, EndOfStreamException {
        if (SpareCapacity() <= 0) {
            if (readOffset == 0)
                throw new IOException("Connection is full (cannot read any more)");

            MoveBufferedDataToOffset();
        }
        int nBytesRead = socket.getInputStream().read(buffer, writeOffset, SpareCapacity());
        if (nBytesRead == -1) throw new EndOfStreamException();
        writeOffset += nBytesRead;
    }

    protected class EndOfStreamException extends Exception {
    }
}
