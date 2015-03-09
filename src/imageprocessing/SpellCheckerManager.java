/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *

package imageprocessing;

import com.swabunga.spell.engine.SpellDictionaryHashMap;
import com.swabunga.spell.event.SpellChecker;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SpellCheckerManager {

    protected static SpellDictionaryHashMap dictionary = null;
    protected static SpellChecker spellChecker = null;

    static {
       try
       {
          dictionary =
                  new SpellDictionaryHashMap(new
                                File("libraries/english.0"));
       } catch (IOException ex)
       {
          Logger.getLogger(SpellCheckerManager.class.getName()).log(Level.SEVERE, null, ex);
       }
        spellChecker = new SpellChecker(dictionary);
    }

    public static List getSuggestions(String word,
        int threshold) {

        return spellChecker.getSuggestions(word, threshold);
    }
}

*/