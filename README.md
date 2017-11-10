# BellChoir

## How I Met the Goals of Lab 2

* [x] My program reads and validates a file. 
* [x] Each ChoirMember can have multiple of the same note,
but only one. 
* [x] I ensure this by using a hashMap to check which notes have already been mapped to a choir member. 
* [x] Each ChoirMember runs on its own thread.
* [x] By using the Conductor as a mutex, I ensure only one thread is playing at
a time. 
* [x] The BellNote is passed to a Choir Member so that it may be played, along with the turn it should be played at.

## Challenges

I was hoping to do the harmony task as well but just didn't have the time for it. 
The biggest challenge for me was making sure they played in the right order, and I'm sure there's a better way to do it than how I did it.
