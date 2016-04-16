package snprinter;

import java.util.Arrays;

/**
 *
 * @author Admin
 */
public class Main {

    /**
     * NB_CHANNELS COMP1,COMP2,COMP3,COMP4,...
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        nbChannels = Short.parseShort(args[0]);
        String[] temp = args[1].split(",");
        input = new short[temp.length];

        for (int i = 0; i < temp.length; i++) {
            input[i] = Short.parseShort(temp[i]);
        }

        if (input.length >= 1) {
            process();
        }
    }

    private static short[] input;
    private static short nbChannels;

    public static void process() {
        short[][] defaultNetwork = getOriginalInputs(0);

        short[][] data = defaultNetwork.clone();

        for (int i = 0; i < input.length; i++) {
            short comp = input[i];

            short[] temp = new short[i + 1];
            System.arraycopy(data[0], 0, temp, 0, data[0].length);
            temp[i] = input[i];

            data[0] = temp;
            processData(data, comp, 1);
            processW(data, comp, 1);

            printData(data);
        }
    }

    public static void printData(short[][] data) {
        System.out.println("Data: ");
        for (int i = 1; i < data.length; i++) {
            System.out.println(printBinary(data[i]));
        }
        System.out.println("");
    }
    
    public static String printBinary(short[] data) {
        StringBuilder str = new StringBuilder();
        str.append("[");
        for(int i = 0; i < data.length; i++) {
            String number = String.format("%5s", Integer.toBinaryString(data[i])).replace(' ', '0');
            //String = Integer.toBinaryString(data[i])
            str.append(number).append(",");
        }
        str.deleteCharAt(str.length()-1);
        str.append("]");
        return str.toString();
    }

    /**
     * Get all original inputs excluding the already sorted ones.
     *
     * @param upperBound unused
     * @return range from 2 to (2^nbChannels-1) excluding all sorted (binary)
     * ones.
     */
    public static short[][] getOriginalInputs(int upperBound) {
        /* 
         data[0] holds the lengths of the other shorts.
         data[1] holds outputs with 1 '1's.
         data[2] holds outputs with 2 '1's.
         ...
         data[n] nbChannels holds W(C,x,k) info.
         */
        short[][] data = new short[nbChannels + 1][];
        data[0] = new short[1];
        data[nbChannels] = new short[(nbChannels - 1) << 2];
        int wIndexCounter;

        for (int nbOnes = 1; nbOnes < nbChannels; nbOnes++) {
            data[nbOnes] = getPermutations((short) ((1 << nbOnes) - 1), nbChannels);
            wIndexCounter = (nbOnes - 1) << 2;

            data[nbChannels][wIndexCounter] = (short) ((1 << nbChannels) - 1);
            data[nbChannels][wIndexCounter + 1] = nbChannels;
            data[nbChannels][wIndexCounter + 2] = (short) ((1 << nbChannels) - 1);
            data[nbChannels][wIndexCounter + 3] = nbChannels;
        }

        return data;
    }

    /**
     * Get all permutations possible using the start and the max amount of bits.
     *
     * @param start The start value. Normally a value with all 1's on the right
     * side.
     * @param maxBits The maximum amount of bits available. (nbChannels)
     * @return All permutations starting with start that are smaller than
     * (2^maxBits)-1
     */
    public static short[] getPermutations(short start, short maxBits) {
        //Calculate length
        int beginNbOnes = Integer.bitCount(start);
        float temp = factorial(maxBits) / factorial(beginNbOnes); //TODO: Could get more efficient
        temp /= factorial(maxBits - beginNbOnes);
        int length = (int) Math.ceil(temp);

        //Variables
        short[] result = new short[length];
        int value = start;
        int max = (1 << maxBits) - 1;
        int t;
        int index = 0;

        //Get all permutations.
        do {
            result[index] = (short) value;
            //System.out.println(Integer.toBinaryString(value)); //-- DEBUG!
            t = value | (value - 1);
            value = (t + 1) | (((~t & -~t) - 1) >> (Integer.numberOfTrailingZeros(value) + 1));
            index++;
        } while (value < max);

        return result;
    }

    /**
     * Calculates the factorial of a given number.
     *
     * @param n The number of which the factorial has to be calculated
     * @return n!
     */
    public static long factorial(int n) {
        long result = 1;

        for (; n > 1; n--) {
            result *= n;
        }

        return result;
    }

    /**
     * TODO
     *
     * @param data
     * @param comp
     * @param startIndex
     */
    public static void processW(short[][] data, short comp, int startIndex) {
        short[] wResult = new short[data[nbChannels].length];

        int wIndexCounter;
        boolean foundL;
        boolean foundP;

        if (startIndex != 1) {
            System.arraycopy(data[nbChannels], 0, wResult, 0, (startIndex - 1) << 2);
        }

        for (int nbOnes = startIndex; nbOnes < nbChannels; nbOnes++) {
            wIndexCounter = (nbOnes - 1) << 2;

            int oldP = data[nbChannels][wIndexCounter];
            int oldL = data[nbChannels][wIndexCounter + 2];

            int P = (comp ^ ((1 << nbChannels) - 1)) & oldP;
            int L = (comp ^ ((1 << nbChannels) - 1)) & oldL;

            foundP = (oldP == P);
            foundL = (oldL == L);

            for (short output : data[nbOnes]) {
                if (!foundL) {
                    L = L | (output & comp);
                    if ((L & comp) == comp) {
                        foundL = true;
                    }
                }

                if (!foundP) {
                    P = P | ((output ^ ((1 << nbChannels) - 1)) & comp);
                    if ((P & comp) == comp) {
                        foundP = true;
                    }
                }

                /* Break; found both */
                if (foundP && foundL) {
                    break;
                }
            }
            wResult[wIndexCounter] = (short) P;
            wResult[wIndexCounter + 1] = (short) Integer.bitCount(P);
            wResult[wIndexCounter + 2] = (short) L;
            wResult[wIndexCounter + 3] = (short) Integer.bitCount(L);
        }
        data[nbChannels] = wResult;
    }

    /**
     * Processes the data for the new comparator. Adding the comparator to the
     * data[0] is assumed to be done already.
     *
     * @param data The network
     * @param newComp The comparator to process the data on.
     * @param startIndex The outerIndex of where to start. (= 1 will cover
     * everything.)
     */
    public static void processData(short[][] data, short newComp, int startIndex) {
        short[] processed;
        boolean found;

        for (int nbOnes = startIndex; nbOnes < nbChannels; nbOnes++) {
            processed = new short[data[nbOnes].length];
            int counter = 0;
            boolean foundNew = false;

            for (int innerIndex = 0; innerIndex < data[nbOnes].length; innerIndex++) {
                short oldValue = data[nbOnes][innerIndex];
                short value = swapCompare(oldValue, newComp);
                if (value != oldValue) {
                    foundNew = true;
                }
                found = false;
                for (int i = counter - 1; i >= 0; i--) {
                    if (processed[i] == value) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    processed[counter++] = value;
                }
            }

            if (foundNew) { //CAUTION! Don't do this 'shared array' with writing lists to disk.
                short[] result = new short[counter];
                System.arraycopy(processed, 0, result, 0, result.length);
                data[nbOnes] = result;
            }
        }

    }

    /**
     * Get the output of the comparator comp given the input.
     *
     * @param input The input to give the comparator.
     * @param comp The comparator to get the output from.
     * @return The result by switching the bits in the input according to comp.
     */
    private static short swapCompare(short input, short comp) {
        int pos1 = 31 - Integer.numberOfLeadingZeros(comp);
        int pos2 = Integer.numberOfTrailingZeros(comp);
        //(input >> pos1) & 1 = first (front bit)
        //(input >> pos2) & 1 = 2nd (back bit)
        return (((input >> pos1) & 1) <= ((input >> pos2) & 1)) ? input : (short) (input ^ comp);// TADAM!!!
    }

}
