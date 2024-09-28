using Hackathon.Hackathon;

namespace Hackathon 
{
    public class HaHackathon
    {
        public List<WishList> RunHackathon(List<Participant> juniors, List<Participant> teamLeads)
        {
            List<WishList> wishLists = new();

            foreach (var teamLead in teamLeads)
            {
                wishLists.Add(teamLead.GenerateWishList(juniors));
            }

            foreach(var junior in juniors)
            {
                wishLists.Add(junior.GenerateWishList(teamLeads));
            }

            return wishLists;
        }



    }
}


