using Hackathon.Hackathon;

namespace Hackathon
{
    class Program
    {
        static void Main(string[] args)
        {
            List<Participant> teamLeads = Parser.Parse("Juniors20.csv", "TeamLead");
            List<Participant> juniors = Parser.Parse("Teamleads20.csv", "Junior");

            HRManager manager = new();
            HRDirector director = new();
            HaHackathon hackathon = new();

            double totalHarmonicity = 0;
            int numberOfHackathons = 1000;
            for (int i = 0; i < numberOfHackathons; i++)
            {
                List<WishList> wishLists = hackathon.RunHackathon(juniors, teamLeads);
                var teams = manager.GetTeams(wishLists);
                var harmonicity = director.CalculateHarmonicity(teams, wishLists);
                totalHarmonicity += harmonicity;
                Console.WriteLine("Harmonicity for hackathon №" + i + " = " + harmonicity);

                CleanFree(juniors);
                CleanFree(teamLeads);
            }

            Console.WriteLine("Total harmonicity = " + (totalHarmonicity / numberOfHackathons));
        }

        public static void CleanFree(List<Participant> participants)
        {
            foreach (var participant in participants)
            {
                participant.IsFree = true;
            }
        }
    }
}