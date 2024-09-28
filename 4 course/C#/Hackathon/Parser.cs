using Hackathon.Hackathon;

namespace Hackathon 
{
    public class Parser 
    {
        public static List<Participant> Parse(string filePath, string type)
        {
            List<Participant> result = new();

            using (StreamReader reader = new(filePath))
            {
                reader.ReadLine();

                string? line;
                while ((line = reader.ReadLine()) != null) 
                {
                    string[] values = line.Split(';');
                    Participant participant = new(int.Parse(values[0]), values[1], type);
                    result.Add(participant);
                }
            }

            return result;
        }
    }
}
