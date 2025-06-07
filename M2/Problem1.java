package M2;
 // UCID: ad273 | Date: 6/7/2025
public class Problem1 extends BaseClass {
    private static int[] array1 = {0,1,2,3,4,5,6,7,8,9};   
    private static int[] array2 = {9,8,7,6,5,4,3,2,1,0};
    private static int[] array3 = {0,0,1,1,2,2,3,3,4,4,5,5,6,6,7,7,8,8,9,9};
    private static int[] array4 = {9,9,8,8,7,7,6,6,5,5,4,4,3,3,2,2,1,1,0,0}; 

    private static void printOdds(int[] arr, int arrayNumber){
        printArrayInfo(arr, arrayNumber);

        System.out.print("Output Array: ");
       
        boolean first = true;
        for (int num : arr) {
            if (num % 2 != 0) {
                if (!first) {
                    System.out.print(", ");
                }
                System.out.print(num);
                first = false;
            }
        }
        // End Solution Edits

        System.out.println("");
        System.out.println("______________________________________");
    }

    public static void main(String[] args) {
        final String ucid = "ad273"; 

        printHeader(ucid, 1);
        printOdds(array1, 1);
        printOdds(array2, 2);
        printOdds(array3, 3);
        printOdds(array4, 4);
        printFooter(ucid, 1);
    }
}
