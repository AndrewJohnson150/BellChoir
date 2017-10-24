import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class ChoirMember extends Thread{
	private List<Note> notes;
	private List<NoteLength> lengths;
	private List<Integer> turns;
	private final AudioFormat af;
	private Thread t;
	private Conductor mutex;
	private int[] turnsAndLengths;
	
	public ChoirMember(Conductor c) {
		af = new AudioFormat(Note.SAMPLE_RATE, 8, 1, true, false);
		t = new Thread(this);
		mutex = c;
		notes = new ArrayList<Note>();
		lengths = new ArrayList<NoteLength>();
		turns = new ArrayList<Integer>();
	}
	
	public void addNoteToPlay(Note n, NoteLength len, int t) {
		notes.add(n);
		lengths.add(len);
		turns.add(t);
	}
	
	public void startPlaying() {
		t.start();
	}
	
	public void run() {
		for (int i = 0; i<notes.size();i++) {
			//acquire the mutex 
			mutex.acquire(turns.get(i));
			
			BellNote noteToPlay = new BellNote(notes.get(i),lengths.get(i));
			try {
				play(noteToPlay);
			} catch (LineUnavailableException ignore) {ignore.printStackTrace();}
			
			//release the mutex
			mutex.release();
		}
	}

	
	void play(BellNote note) throws LineUnavailableException {
        try (final SourceDataLine line = AudioSystem.getSourceDataLine(af)) {
            line.open();
            line.start();
            playNote(line, note);
            line.drain();
        }
    }
	
    private void playNote(SourceDataLine line, BellNote bn) {
        final int ms = Math.min(bn.length.timeMs(), Note.MEASURE_LENGTH_SEC * 1000);
        final int length = Note.SAMPLE_RATE * ms / 1000;
        line.write(bn.note.sample(), 0, length);
        line.write(Note.REST.sample(), 0, 50);
    }
    
    public void concludeMusicSession() {
    	try {
			t.join();
		} catch (InterruptedException e) {e.printStackTrace();}
    }


}



