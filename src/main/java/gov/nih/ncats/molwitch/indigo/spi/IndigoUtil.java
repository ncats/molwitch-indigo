/*
 * NCATS-MOLWITCH-INDIGO
 *
 * Copyright 2020 NIH/NCATS
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package gov.nih.ncats.molwitch.indigo.spi;

import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoInchi;
import com.epam.indigo.IndigoObject;
import gov.nih.ncats.molwitch.Chirality;

import java.util.OptionalInt;

public class IndigoUtil {
    //need to have a Indigo object per thread
    private static ThreadLocal<Indigo> indigoThreadLocal = ThreadLocal.withInitial(()->{
        Indigo indigo = new Indigo();
        //by default indigo sets mol files to auto which writes v2000 unless it needs to
        //which is very reasonable, but doesn't match molwitch which writes to 2000 unless told not to.
        //by defaulting to 2000 we should save a lot of unessessary cloning of Indigo objects to reset settings
        System.out.println("initial version of molfile saving mode = " + indigo.getOption("molfile-saving-mode"));
//        indigo.setOption("molfile-saving-mode", "2000");
        return indigo;
    });
    private static ThreadLocal<IndigoInchi> indigoInchiThreadLocal = ThreadLocal.withInitial(()->new IndigoInchi(indigoThreadLocal.get()));


    public static IndigoInchi getIndigoInchiInstance(){
        return indigoInchiThreadLocal.get();
    }

    public static Indigo getIndigoInstance(){
        return indigoThreadLocal.get();
    }

    public static Chirality getIndigoChiralityForAtom(IndigoObject chiralAtom){
        /*
            comments from Indigo C source: note that this is backwards from molwitch
            //parity = 1  if [2]-nd substituent is rotated CCW w.r.t. [0]-th
            //             substituent when we look at it from "left" to "right"
            // parity = 2  if it is rotated CW
             */
        int indigoParity = chiralAtom.checkChirality();
        if(indigoParity ==1){
            return Chirality.S;
        }
        if(indigoParity ==2){
            return Chirality.R;
        }


        return Chirality.valueByParity(chiralAtom.checkChirality());
    }

    public static int valueOrZero(Integer v){
        if( v==null){
            return 0;
        }
        return v;
    }
    public static OptionalInt valueOrEmpty(Integer v){
        if( v==null){
            return OptionalInt.empty();
        }
        return OptionalInt.of(v);
    }
}
