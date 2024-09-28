using Hackathon.Hackathon;

namespace Hackathon
{
    public class HRDirector
    {
        public double GetSatisfactionIndex(Participant assignedParticipant, WishList wishList)
        {
            return 20 - wishList.PriorityList.IndexOf(assignedParticipant);
        }

        public double CalculateHarmonicity(List<Team> teams, List<WishList> wishLists)
        {
            double totalHarmonicity = 0;
            int totalParticipants = teams.Count * 2;

            foreach (var team in teams)
            {
                var juniorWishList = wishLists.FirstOrDefault(w => w.Owner == team.Junior);
                var teamLeadWishList = wishLists.FirstOrDefault(w => w.Owner == team.TeamLead);

                if (juniorWishList == null || teamLeadWishList == null)
                {
                    throw new Exception("Не найдены WishList для участников команды.");
                }

                double juniorSatisfaction = GetSatisfactionIndex(team.TeamLead, juniorWishList);
                double teamLeadSatisfaction = GetSatisfactionIndex(team.Junior, teamLeadWishList);

                totalHarmonicity += (juniorSatisfaction + teamLeadSatisfaction) / (juniorSatisfaction * teamLeadSatisfaction);
            }

            return (double)totalParticipants / totalHarmonicity; 
        }
    }
}
