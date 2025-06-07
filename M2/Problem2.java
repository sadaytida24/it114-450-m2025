package M2;

public class Problem2 extends BaseClass {
    private static double[] array1 = {0.5, 1.5, 2.0, 3.0, 4.75};
    private static double[] array2 = {2.22, 3.33, 4.44, 5.55, 6.66};
    private static double[] array3 = {1.1, 2.2, 3.3, 4.4, 5.5};
    private static double[] array4 = {9.99, 8.88, 7.77, 6.66, 5.55};

    private static void sumValues(double[] arr, int arrayNumber){
        printArrayInfo(arr, arrayNumber);

        System.out.print("Output Sum: ");

        double sum = 0.0;
        for (double num : arr) {
            sum += num;
        }
        System.out.printf("%.2f", sum);
        // End Solution Edits

        System.out.println("");
        System.out.println("______________________________________");
    }

    public static void main(String[] args) {
        final String ucid = "ad273";
        printHeader(ucid, 2);
        sumValues(array1, 1);
        sumValues(array2, 2);
        sumValues(array3, 3);
        sumValues(array4, 4);
        printFooter(ucid, 2);
    }
}
