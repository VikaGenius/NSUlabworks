using Hackathon.Hackathon;

namespace Hackathon 
{
    public class HRManager
    {
        public List<Team> GetTeams(List<WishList> wishLists) {
            return SampleStrategy.FormTeams(wishLists);
        }
    }
}

