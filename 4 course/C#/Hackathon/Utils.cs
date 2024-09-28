namespace Hackathon {
    public class Utils {
        public static List<T> ShuffleList<T>(List<T> list)
        {
            Random random = new();
            return list.OrderBy(x => random.Next()).ToList();
        }
    }
}