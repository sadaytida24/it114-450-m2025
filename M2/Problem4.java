package M2;

public class Problem4 extends BaseClass {
    private static String[] array1 = { "hello world!", "java programming", "special@#$%^&characters", "numbers 123 456",
            "mIxEd CaSe InPut!" };
    private static String[] array2 = { "hello world", "java programming", "this is a title case test",
            "capitalize every word", "mixEd CASE input" };
    private static String[] array3 = { "  hello   world  ", "java    programming  ",
            "  extra    spaces  between   words   ",
            "      leading and trailing spaces      ", "multiple      spaces" };
    private static String[] array4 = { "hello world", "java programming", "short", "a", "even" };

    private static void transformText(String[] arr, int arrayNumber) {
        printArrayInfoBasic(arr, arrayNumber);

        for (int i = 0; i < arr.length; i++) {
            String placeholderForModifiedPhrase = "";
            String placeholderForMiddleCharacters = "";

            String phrase = arr[i];

            phrase = phrase.replaceAll("[^a-zA-Z0-9 ]", "");

            phrase = phrase.trim().replaceAll("\\s+", " ");

            String[] words = phrase.split(" ");
            StringBuilder sb = new StringBuilder();
            for (String word : words) {
                if (word.length() > 0) {
                    sb.append(Character.toUpperCase(word.charAt(0)));
                    if (word.length() > 1) {
                        sb.append(word.substring(1).toLowerCase());
                    }
                    sb.append(" ");
                }
            }
            placeholderForModifiedPhrase = sb.toString().trim();

            String noSpaces = placeholderForModifiedPhrase.replace(" ", "");
            if (noSpaces.length() >= 3) {
                int mid = noSpaces.length() / 2;
                placeholderForMiddleCharacters = noSpaces.substring(mid - 1, mid + 2);
            } else {
                placeholderForMiddleCharacters = "Not enough characters";
            }

            System.out.println(String.format("Index[%d] \"%s\" | Middle: \"%s\"", i, placeholderForModifiedPhrase,
                    placeholderForMiddleCharacters));
        }

        System.out.println("\n______________________________________");
    }

    public static void main(String[] args) {
        final String ucid = "ad273";
        printHeader(ucid, 4);

        transformText(array1, 1);
        transformText(array2, 2);
        transformText(array3, 3);
        transformText(array4, 4);
        printFooter(ucid, 4);
    }
}
